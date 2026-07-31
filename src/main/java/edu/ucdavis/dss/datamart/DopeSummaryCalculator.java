package edu.ucdavis.dss.datamart;

import edu.ucdavis.dss.datamart.dto.DopeRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Person-first tally of DOPE records. Within a job code and month, a person's FTE is the max across
 * funding-split lines; within an instructor type those monthly values are summed across the person's job
 * codes; and their FTE is then their **appointment level**, the highest monthly value (see peakFte).
 * People are deduplicated again when job codes roll
 * up to an instructor type, so headcount counts a person once while their FTE and pay add.
 */
public class DopeSummaryCalculator {
    public static final String UNMAPPED = "Unmapped";

    /*
     * Maps DOPE job code descriptions to IPA InstructorType descriptions (see V189/V202/V244/V248
     * migrations), plus TAs/Readers which are separate budget report lines. Keyed by description
     * during prototyping; unmapped codes are reported with their numeric job code so this can
     * become a job-code-keyed map once ratified by the Dean's Office.
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
        // RECALL FACULTY was mapped here and removed 2026-07-29. Recalled faculty on this title
        // predominantly supervise special study, group study and seminars — instruction that is real but
        // not in the teaching budget — so it does not belong in a category compared against plan. It was
        // 85% of the Emeriti line ($390,051 of $460,737 in FY2026) and the sole content of that line in
        // five departments, so its variance there was entirely fabricated. Now falls to Unmapped.
        // NOT excluded as "research": at 29.9% of compensation in Jul-Sep it is paid on the 12-month
        // academic-year spread (PROF-AY is 26.6%), nothing like summer research salary
        // (RES-LR SCL-AY-1/9 is 85.1%). It is teaching, just not budgeted teaching.
        Map.entry("RECALL TEACHING", "Emeriti - Recalled"),
        Map.entry("PROF EMERITUS(WOS)", "Emeriti - Recalled"),
        Map.entry("RECALL TEACHING NON-SENATE", "Emeriti - Recalled"),
        Map.entry("LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("SR LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("LECT-AY-1/9-CONTINUING", "Continuing Lecturer"),
        // Continuing-lecturer augmentation is a lecturer asked to teach an extra course — still a
        // lecturer title, and temporary, so it reports with Unit 18 rather than as its own line
        // (2026-07-29, confirmed against prior Dean's Office correspondence). Unit 18 already holds
        // PRE-SIX YR APPT-TEMP SUPP-*, the pre-six analogue of the same thing. Rolling a *continuing*
        // appointment up to pre-six reads odd, which is why this was briefly reverted and reinstated —
        // don't "fix" it again without checking that correspondence. The plan side is merged to match;
        // see BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE, and note the two must move together or these actuals
        // land in no reported category at all.
        Map.entry("CONTINUING APPT-TEMP AUG-AY", "Unit 18 Pre-Six Lecturer"),
        Map.entry("CONTINUING APPT-TEMP AUG-1/9", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-AY", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-AY-1/9", "Unit 18 Pre-Six Lecturer"),
        // LECT-MISCELLANEOUS/PART TIME was mapped here and removed 2026-07-28: it carries 0.00 FTE in
        // every row of both FY2025 and FY2026 (21 department-years, 31 people) at ~$700-1,000 a head,
        // against $16.8k-$44.6k per person for the real Unit 18 codes. It is an honorarium-scale
        // payment, not an appointment, and it was inflating Unit 18 headcount without adding FTE.
        // Now falls to Unmapped, where it stays visible with its numeric job code.
        Map.entry("PRE-SIX YR APPT-TEMP SUPP-1/9", "Unit 18 Pre-Six Lecturer"),
        Map.entry("PRE-SIX YR APPT-TEMP SUPP-1/10", "Unit 18 Pre-Six Lecturer"),
        Map.entry("ASSOC IN __ -AY-1/9-GSHIP", "Associate Instructor"),
        Map.entry("TEACHG ASST-GSHIP", "TAs"),
        Map.entry("TEACHG ASST-GSHIP/NON REP", "TAs"),
        Map.entry("READER-GSHIP", "Readers"),
        Map.entry("READER-NON GSHIP", "Readers"),
        Map.entry("READER-NON STDNT", "Readers"));

    /* per person within one job code or instructor type */
    private static class PersonTally {
        /* staging, collapsed into fteByMonth by settleFte() once every row has been seen. Position is a
           key only so a reversal can cancel the appointment it reverses; it never leaves this class. */
        Map<Integer, Map<String, BigDecimal>> fteCandidates = new HashMap<>();
        Map<Integer, Map<String, BigDecimal>> netByMonthPosition = new HashMap<>();

        Map<Integer, BigDecimal> fteByMonth = new HashMap<>();

        /**
         * Each month's FTE, summed across positions, dropping any position-month whose pay nets to zero
         * or less — a reversed appointment was never held.
         *
         * English FY2026 is the case: a 0.86 `LECT-AY-1/9` was paid for October and November, then
         * cancelled in December by two matching negative rows and restated as a 0.57 `LECT-AY` on a new
         * position. Counting both read **1.43 FTE** for one person holding 0.57.
         *
         * A position-month with no monetary row at all is kept: absent money is not a reversal.
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

    /* true when this job code belongs to a summer-bearing student category (TA/AI/Reader), so its
       Jul-Sep pay is Summer Session and should be dropped from an academic-year figure */
    public static boolean isSummerSessionBearing(String jobCodeDescription) {
        if (jobCodeDescription == null) {
            return false;
        }
        String instructorType = instructorTypeFor(jobCodeDescription);
        return instructorType != null && SUMMER_SESSION_BEARING_TYPES.contains(instructorType);
    }

    /**
     * True for titles paid over the nine academic-year months rather than spread across twelve, which
     * the "-1/9" suffix marks. Their Jul-Sep pay cannot be deferred academic-year salary — they are not
     * paid in those months at all — so it is additional summer work.
     *
     * Evidence (FY2026, L&S-wide, share of compensation falling in Jul-Sep): plain -AY titles cluster
     * at 24-28%, almost exactly 3/12 — PROF-AY 26.6%, ASSOC PROF-AY 26.5%, LECT-AY 24.0%,
     * LECT-AY-CONTINUING 25.5%. The instructional 1/9 titles sit far below: ASST PROF-AY-1/9 0.0%,
     * LECT-AY-1/9-CONTINUING 0.0%, LECT-AY-1/9 5.2%, CONTINUING APPT-TEMP AUG-1/9 12.2%.
     *
     * This deliberately also catches the RES-LR SCL-AY-1/9 research family (148 people, 84.8% of
     * compensation in Jul-Sep — nine-month researchers taking summer salary). Those titles are all
     * Unmapped, so no compared figure moves; the effect is that ~$3.7M of summer research compensation
     * leaves the Unmapped row's academic-year salary L&S-wide, which is right — it is summer research
     * pay, not instruction — and stays visible in the Summer Salary column.
     *
     * Note PRE-SIX YR APPT-TEMP SUPP-1/10 is NOT matched: ten-month pay presumably leaves two unpaid
     * months, but which two is unevidenced, so it is left on the 12-month assumption deliberately.
     */
    private static boolean isNineMonthPaid(String jobCodeDescription) {
        return jobCodeDescription != null && jobCodeDescription.contains("1/9");
    }

    /**
     * Summer Session pay, excluded from academic-year figures. THE single rule — both views read it,
     * so By Category and By Course cannot drift apart. Jul-Sep pay is Summer Session when either
     * the category or the pay basis says the person is not paid across the summer:
     *
     * (a) explicit Summer Session job codes (e.g. LECT IN SUMMER SESSION) are summer whatever the
     *     month; (b) the summer-bearing student categories (TA/AI/Reader), whose regular pay starts in
     *     October; and (c) any title paid 1/9 — see isNineMonthPaid. Plain -AY faculty and lecturer pay
     *     is kept whole, since it is spread across the summer months rather than earned in them.
     */
    public static boolean isSummerPay(String jobCodeDescription, int fiscalMonth) {
        if (jobCodeDescription != null && jobCodeDescription.toUpperCase().contains("SUMMER")) {
            return true;
        }
        return fiscalMonth <= 3
            && (isSummerSessionBearing(jobCodeDescription) || isNineMonthPaid(jobCodeDescription));
    }

    /**
     * The fiscal month the pay was **earned** in, for bucketing FTE. `FISCAL_MONTH` is when the
     * transaction *posted*, which is not the same thing: a retroactive payment carries its appointment
     * forward into a later month.
     *
     * PSC FY2026 is the case that forced this. A 0.83 pre-six appointment (`LECT-AY`) ended in August,
     * but one of its July-earned rows posted in fiscal month 4. Bucketed on the posting month it looked
     * like a live October appointment and added to the person's real 0.67 continuing appointment, so a
     * Fall course row read **1.50 FTE** for someone who held 0.67. Neither summing nor taking a max
     * fixes that — 0.83 was simply not an October appointment.
     *
     * Returns null when the earn date is absent, or when it belongs to a different fiscal year: a
     * cross-year retro is last year's appointment level and must not land in this year's terms.
     * Callers skip the row's FTE rather than guess a month for it.
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
        // intermediate tally: job code -> person -> monthly FTE and compensation
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
            // Bucketed on the month the pay was EARNED, not the month it posted — see earnedFiscalMonth.
            // A retro row (earn month != posting month) may not ESTABLISH an appointment month; it only
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

        // every row has been seen, so each person's candidate months can now be netted and collapsed —
        // must happen before the rollup below reads fteByMonth
        for (Map<String, PersonTally> byPerson : peopleByJobCode.values()) {
            byPerson.values().forEach(PersonTally::settleFte);
        }

        // roll up people into job code tallies; person FTE = appointment level (see peakFte).
        // instructor type rollup dedupes people again: one person with two job codes in the
        // same type counts once for headcount while their FTE and compensation add.
        Map<String, DopeTotals> byJobCodeDescription = new HashMap<>();
        Map<String, Map<String, PersonTally>> peopleByInstructorType = new HashMap<>();
        Set<String> employeeIds = new HashSet<>();

        for (Map.Entry<String, Map<String, PersonTally>> jobCodeEntry : peopleByJobCode.entrySet()) {
            DopeTotals tally = new DopeTotals();
            String instructorType = instructorTypeFor(jobCodeEntry.getKey()) != null
                ? instructorTypeFor(jobCodeEntry.getKey()) : UNMAPPED;

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
                // not double-count the duration. A title change with a mid-month effective date would
                // double-count here; not guarded, because no instance has turned up and the
                // Term FTE > 1.00 scan over all 49 workbooks would surface one.
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
     * A person's **appointment level**: the highest monthly FTE they held in the period. A half-time
     * appointment reads 0.50 whether it lasted one quarter or three, and a full-time one reads 1.00.
     * academicYearOnly ignores Jul-Sep.
     *
     * Chosen over volume (FTE-years or FTE-quarters) on 2026-07-29 because volume produces figures that
     * do not read as FTE — a full-time year-round person is 3.00 FTE-quarters, and a half-time one-quarter
     * appointment is 0.17 FTE-years. Max also avoids the blend that made the previous average-over-months
     * wrong: someone at 0.25 in fall and 0.5 in winter reads 0.50, their actual appointment, not 0.38.
     *
     * **This is a level, not a volume, with two consequences.** Summing it across people gives total
     * appointment level present — which is how Rec #5 talks ("2.0 FTE were present and teaching") — but
     * it is blind to duration, so a one-quarter and a three-quarter TA at the same percentage contribute
     * equally despite costing very differently. It therefore **cannot** serve the SIB doc's step 6
     * method, FTE × standard rate; that needs volume, and would need a second figure alongside this one.
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

    /* DOPE rows without a position number still need a bucket; they group together rather than each
       counting as its own appointment, which would re-introduce the double count this key prevents */
    private static String positionOf(DopeRecord record) {
        return record.getPositionNumber() != null ? record.getPositionNumber() : "(none)";
    }
}
