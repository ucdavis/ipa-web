package edu.ucdavis.dss.datamart;

import edu.ucdavis.dss.datamart.dto.DopeRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Person-first tally of DOPE records. Within a position and month, a person's FTE is the max across
 * funding-split lines; positions and job codes then add, and their FTE is their appointment level,
 * the highest monthly value (see peakFte). Headcount counts a person once per category while their
 * FTE and pay add.
 */
public class DopeSummaryCalculator {
    public static final String UNMAPPED = "Unmapped";

    /*
     * Maps DOPE job code descriptions to IPA InstructorType descriptions, plus TAs/Readers which are
     * separate budget report lines. Unmapped codes are reported with their numeric job code.
     */
    private static final Map<String, String> INSTRUCTOR_TYPE_BY_JOB_CODE_DESCRIPTION = Map.ofEntries(
        Map.entry("PROF-AY", "Ladder Faculty"),
        Map.entry("PROF-FY", "Ladder Faculty"),
        Map.entry("ASSOC PROF-AY", "Ladder Faculty"),
        Map.entry("ASST PROF-AY", "Ladder Faculty"),
        Map.entry("ASST PROF-AY-1/9", "Ladder Faculty"),
        Map.entry("ASST PROF-FY", "Ladder Faculty"),
        // Professor of Teaching series = renamed LSOE series
        Map.entry("PROF OF TEACH-AY", "Lecturer SOE"),
        Map.entry("ASSOC PROF OF TEACH-AY", "Lecturer SOE"),
        Map.entry("ASST PROF OF TEACH-AY", "Lecturer SOE"),
        Map.entry("VIS PROF", "Visiting Professor"),
        Map.entry("VIS ASSOC PROF", "Visiting Professor"),
        Map.entry("VIS ASST PROF", "Visiting Professor"),
        // RECALL FACULTY is deliberately unmapped. Recalled faculty on this title mostly supervise special
        // study, group study and seminars: real instruction, but not in the teaching budget. It is not
        // summer research either; its pay follows the 12-month academic-year spread.
        Map.entry("RECALL TEACHING", "Emeriti - Recalled"),
        Map.entry("PROF EMERITUS(WOS)", "Emeriti - Recalled"),
        Map.entry("RECALL TEACHING NON-SENATE", "Emeriti - Recalled"),
        Map.entry("LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("SR LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("LECT-AY-1/9-CONTINUING", "Continuing Lecturer"),
        // Continuing-lecturer augmentation is a lecturer asked to teach an extra course, temporarily, so it
        // reports with Unit 18, like PRE-SIX YR APPT-TEMP SUPP-*. This follows Dean's Office correspondence;
        // check it before changing this. BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE merges the plan side to match, so
        // the two must change together.
        Map.entry("CONTINUING APPT-TEMP AUG-AY", "Unit 18 Pre-Six Lecturer"),
        Map.entry("CONTINUING APPT-TEMP AUG-1/9", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-AY", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-AY-1/9", "Unit 18 Pre-Six Lecturer"),
        // LECT-MISCELLANEOUS/PART TIME is deliberately unmapped: it is an honorarium-scale payment with
        // 0.00 FTE, not an appointment, and would inflate Unit 18 headcount.
        Map.entry("PRE-SIX YR APPT-TEMP SUPP-1/9", "Unit 18 Pre-Six Lecturer"),
        Map.entry("PRE-SIX YR APPT-TEMP SUPP-1/10", "Unit 18 Pre-Six Lecturer"),
        Map.entry("ASSOC IN __ -AY-1/9-GSHIP", "Associate Instructor"),
        Map.entry("TEACHG ASST-GSHIP", "TAs"),
        Map.entry("TEACHG ASST-GSHIP/NON REP", "TAs"),
        Map.entry("READER-GSHIP", "Readers"),
        Map.entry("READER-NON GSHIP", "Readers"),
        Map.entry("READER-NON STDNT", "Readers"));

    private static class PersonTally {
        /* keyed by position only so a reversal can cancel the appointment it reverses */
        Map<Integer, Map<String, BigDecimal>> fteCandidates = new HashMap<>();
        Map<Integer, Map<String, BigDecimal>> netByMonthPosition = new HashMap<>();

        Map<Integer, BigDecimal> fteByMonth = new HashMap<>();

        /**
         * Each month's FTE, summed across positions, dropping any position-month whose pay nets to zero
         * or less: a reversed appointment was never held. A position-month with no monetary row at all
         * is kept, because absent money is not a reversal.
         */
        void settleFte() {
            for (Map.Entry<Integer, Map<String, BigDecimal>> monthEntry : fteCandidates.entrySet()) {
                BigDecimal monthFte = BigDecimal.ZERO;
                for (Map.Entry<String, BigDecimal> position : monthEntry.getValue().entrySet()) {
                    BigDecimal net = netByMonthPosition
                        .getOrDefault(monthEntry.getKey(), Map.of()).get(position.getKey());
                    if (net == null || net.signum() > 0) {
                        monthFte = monthFte.add(position.getValue());
                    }
                }
                if (monthFte.signum() > 0) {
                    fteByMonth.put(monthEntry.getKey(), monthFte);
                }
            }
        }

        boolean academicYearPresent = false;
        BigDecimal totalCompensation = BigDecimal.ZERO;
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        BigDecimal julSepCompensation = BigDecimal.ZERO;
        BigDecimal julSepSalary = BigDecimal.ZERO;
    }

    /* instructor types whose regular-year pay starts in October, so Jul-Sep pay is Summer Session
       (Dean's Office Rec #1). Faculty/lecturers are 12-month-spread and are NOT summer-bearing. */
    private static final Set<String> SUMMER_SESSION_BEARING_TYPES =
        Set.of("TAs", "Associate Instructor", "Readers");

    /* B/E/E salary-scale variants (e.g. PROF-AY-B/E/E) map like their base titles */
    public static String instructorTypeFor(String jobCodeDescription) {
        String normalized = jobCodeDescription.endsWith("-B/E/E")
            ? jobCodeDescription.substring(0, jobCodeDescription.length() - "-B/E/E".length())
            : jobCodeDescription;
        return INSTRUCTOR_TYPE_BY_JOB_CODE_DESCRIPTION.get(normalized);
    }

    public static boolean isSummerSessionBearing(String jobCodeDescription) {
        if (jobCodeDescription == null) {
            return false;
        }
        String instructorType = instructorTypeFor(jobCodeDescription);
        return instructorType != null && SUMMER_SESSION_BEARING_TYPES.contains(instructorType);
    }

    /**
     * True for titles paid over the nine academic-year months rather than spread across twelve, which
     * the "-1/9" suffix marks. They are not paid in Jul-Sep at all, so pay in those months is summer
     * work, not deferred academic-year salary.
     *
     * This also catches the RES-LR SCL-AY-1/9 research titles. They are Unmapped, so only the Unmapped
     * row's academic-year salary changes. PRE-SIX YR APPT-TEMP SUPP-1/10 is deliberately not matched:
     * which two months ten-month pay leaves unpaid is unknown.
     */
    private static boolean isNineMonthPaid(String jobCodeDescription) {
        return jobCodeDescription != null && jobCodeDescription.contains("1/9");
    }

    /**
     * Summer Session pay, excluded from academic-year figures. Both views use this one rule, so By
     * Category and By Course cannot drift apart. Summer Session job codes are summer in any month. Jul-Sep
     * pay is summer for the student categories (TA/AI/Reader), whose regular pay starts in October, and
     * for 1/9 titles. Plain -AY faculty and lecturer pay is kept whole, since it is spread across the
     * summer months rather than earned in them.
     */
    public static boolean isSummerPay(String jobCodeDescription, int fiscalMonth) {
        if (jobCodeDescription != null && jobCodeDescription.toUpperCase().contains("SUMMER")) {
            return true;
        }
        return fiscalMonth <= 3
            && (isSummerSessionBearing(jobCodeDescription) || isNineMonthPaid(jobCodeDescription));
    }

    /**
     * The fiscal month the pay was earned in, for bucketing FTE. FISCAL_MONTH is when the transaction
     * posted; a retroactive payment posts in a later month and would make a closed appointment look
     * live. Falls back to the posting month when the earn date is missing. Returns null when the earn
     * date is in a different fiscal year: a cross-year retro is last year's appointment.
     */
    public static Integer earnedFiscalMonth(DopeRecord record) {
        LocalDate earned = record.getUcEarnEndDate();
        if (earned == null) {
            return record.getFiscalMonth();
        }
        if (fiscalYearOf(earned) != record.getFiscalYear()) {
            return null;
        }
        return ((earned.getMonthValue() - 7 + 12) % 12) + 1;
    }

    /* the fiscal year runs Jul-Jun and is named for the calendar year it ends in */
    private static int fiscalYearOf(LocalDate date) {
        return date.getMonthValue() >= 7 ? date.getYear() + 1 : date.getYear();
    }

    public static DopeSummary calculate(List<DopeRecord> dopeRecords) {
        Map<String, Map<String, PersonTally>> peopleByJobCode = new HashMap<>();
        SortedSet<String> unmappedJobCodes = new TreeSet<>();

        for (DopeRecord dopeRecord : dopeRecords) {
            String jobCodeDescription =
                dopeRecord.getJobCodeDescription() != null ? dopeRecord.getJobCodeDescription() : "(none)";
            PersonTally personTally = peopleByJobCode
                .computeIfAbsent(jobCodeDescription, k -> new HashMap<>())
                .computeIfAbsent(dopeRecord.getEmployeeId(), k -> new PersonTally());

            if (instructorTypeFor(jobCodeDescription) == null) {
                unmappedJobCodes.add(dopeRecord.getJobCode() + " " + jobCodeDescription);
            }
            if (dopeRecord.getFiscalMonth() >= 4) {
                personTally.academicYearPresent = true;
            }
            // A retro row (earn month != posting month) may not establish an appointment month; it only
            // nets into one, so a retroactive adjustment cannot make a closed appointment look live.
            Integer earnedMonth = earnedFiscalMonth(dopeRecord);
            boolean retro = earnedMonth != null && earnedMonth.intValue() != dopeRecord.getFiscalMonth();
            if (dopeRecord.getFte() != null && earnedMonth != null && !retro) {
                personTally.fteCandidates
                    .computeIfAbsent(earnedMonth, k -> new HashMap<>())
                    .merge(positionOf(dopeRecord), dopeRecord.getFte(), BigDecimal::max);
            }
            // every row nets, retro included — that is how a reversal cancels the month it reverses
            if (dopeRecord.getMonetaryAmount() != null && earnedMonth != null) {
                personTally.netByMonthPosition
                    .computeIfAbsent(earnedMonth, k -> new HashMap<>())
                    .merge(positionOf(dopeRecord), dopeRecord.getMonetaryAmount(), BigDecimal::add);
            }
            if (dopeRecord.getMonetaryAmount() != null) {
                boolean isSalary = "SALARY".equals(dopeRecord.getExpenseType());
                personTally.totalCompensation = personTally.totalCompensation.add(dopeRecord.getMonetaryAmount());
                if (isSalary) {
                    personTally.salary = personTally.salary.add(dopeRecord.getMonetaryAmount());
                    if (isSummerPay(jobCodeDescription, dopeRecord.getFiscalMonth())) {
                        personTally.summerSalary = personTally.summerSalary.add(dopeRecord.getMonetaryAmount());
                    }
                }
                if (dopeRecord.getFiscalMonth() <= 3) {
                    personTally.julSepCompensation = personTally.julSepCompensation.add(dopeRecord.getMonetaryAmount());
                    if (isSalary) {
                        personTally.julSepSalary = personTally.julSepSalary.add(dopeRecord.getMonetaryAmount());
                    }
                }
            }
        }

        for (Map<String, PersonTally> byPerson : peopleByJobCode.values()) {
            byPerson.values().forEach(PersonTally::settleFte);
        }

        Map<String, DopeTotals> byJobCodeDescription = new HashMap<>();
        Map<String, Map<String, PersonTally>> peopleByInstructorType = new HashMap<>();
        Set<String> employeeIds = new HashSet<>();

        for (Map.Entry<String, Map<String, PersonTally>> jobCodeEntry : peopleByJobCode.entrySet()) {
            DopeTotals tally = new DopeTotals();
            String instructorType = Objects.requireNonNullElse(instructorTypeFor(jobCodeEntry.getKey()), UNMAPPED);

            for (Map.Entry<String, PersonTally> personEntry : jobCodeEntry.getValue().entrySet()) {
                PersonTally personTally = personEntry.getValue();
                employeeIds.add(personEntry.getKey());

                addPerson(tally, personTally, peakFte(personTally.fteByMonth, false),
                    peakFte(personTally.fteByMonth, true));

                PersonTally typePerson = peopleByInstructorType
                    .computeIfAbsent(instructorType, k -> new HashMap<>())
                    .computeIfAbsent(personEntry.getKey(), k -> new PersonTally());
                // monthly FTE is summed across the person's job codes within this instructor type, so
                // two concurrent titles add up in a month while a sequential move between titles does
                // not double-count. A title change with a mid-month effective date would double-count.
                personTally.fteByMonth.forEach(
                    (month, fte) -> typePerson.fteByMonth.merge(month, fte, BigDecimal::add));
                typePerson.academicYearPresent = typePerson.academicYearPresent || personTally.academicYearPresent;
                typePerson.totalCompensation = typePerson.totalCompensation.add(personTally.totalCompensation);
                typePerson.salary = typePerson.salary.add(personTally.salary);
                typePerson.summerSalary = typePerson.summerSalary.add(personTally.summerSalary);
                typePerson.julSepCompensation = typePerson.julSepCompensation.add(personTally.julSepCompensation);
                typePerson.julSepSalary = typePerson.julSepSalary.add(personTally.julSepSalary);
            }

            byJobCodeDescription.put(jobCodeEntry.getKey(), tally);
        }

        Map<String, DopeTotals> byInstructorType = new HashMap<>();
        for (Map.Entry<String, Map<String, PersonTally>> typeEntry : peopleByInstructorType.entrySet()) {
            DopeTotals tally = new DopeTotals();
            for (PersonTally person : typeEntry.getValue().values()) {
                addPerson(tally, person, peakFte(person.fteByMonth, false),
                    peakFte(person.fteByMonth, true));
            }
            byInstructorType.put(typeEntry.getKey(), tally);
        }

        return new DopeSummary(byJobCodeDescription, byInstructorType, unmappedJobCodes, employeeIds.size());
    }

    private static void addPerson(DopeTotals tally, PersonTally personTally,
                                  BigDecimal personFte, BigDecimal personAcademicYearFte) {
        tally.people++;
        tally.fte = tally.fte.add(personFte);
        tally.academicYearFte = tally.academicYearFte.add(personAcademicYearFte);
        if (personTally.academicYearPresent) {
            tally.academicYearPeople++;
        }
        tally.totalCompensation = tally.totalCompensation.add(personTally.totalCompensation);
        tally.salary = tally.salary.add(personTally.salary);
        tally.summerSalary = tally.summerSalary.add(personTally.summerSalary);
        tally.julSepCompensation = tally.julSepCompensation.add(personTally.julSepCompensation);
        tally.julSepSalary = tally.julSepSalary.add(personTally.julSepSalary);
    }

    /**
     * A person's appointment level: the highest monthly FTE they held in the period. A half-time
     * appointment reads 0.50 whether it lasted one quarter or three. academicYearOnly ignores Jul-Sep.
     * Max rather than an average: 0.25 in fall and 0.50 in winter reads 0.50, the actual appointment,
     * not a blended 0.38.
     *
     * This is a level, not a volume. It is blind to duration, so it cannot be multiplied by a standard
     * rate (the SIB doc's step 6 method); that would need a second, volume-based figure.
     */
    private static BigDecimal peakFte(Map<Integer, BigDecimal> fteByMonth, boolean academicYearOnly) {
        BigDecimal peak = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> monthEntry : fteByMonth.entrySet()) {
            if (academicYearOnly && monthEntry.getKey() <= 3) {
                continue;
            }
            peak = peak.max(monthEntry.getValue());
        }
        return peak.setScale(2, RoundingMode.HALF_UP);
    }

    /* rows without a position number share one bucket rather than each counting as its own appointment */
    private static String positionOf(DopeRecord record) {
        return record.getPositionNumber() != null ? record.getPositionNumber() : "(none)";
    }
}
