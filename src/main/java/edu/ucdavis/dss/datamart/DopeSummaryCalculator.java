package edu.ucdavis.dss.datamart;

import edu.ucdavis.dss.datamart.dto.DopeRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Person-first tally of DOPE records: within a job code, a person's monthly FTE is the max
 * across funding-split lines and their FTE is the average over months present; people are
 * deduplicated again when job codes roll up to an instructor type.
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
        Map.entry("RECALL FACULTY", "Emeriti - Recalled"),
        Map.entry("RECALL TEACHING", "Emeriti - Recalled"),
        Map.entry("PROF EMERITUS(WOS)", "Emeriti - Recalled"),
        Map.entry("RECALL TEACHING NON-SENATE", "Emeriti - Recalled"),
        Map.entry("LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("SR LECT-AY-CONTINUING", "Continuing Lecturer"),
        Map.entry("LECT-AY-1/9-CONTINUING", "Continuing Lecturer"),
        Map.entry("CONTINUING APPT-TEMP AUG-AY", "Continuing Lecturer - Augmentation"),
        Map.entry("CONTINUING APPT-TEMP AUG-1/9", "Continuing Lecturer - Augmentation"),
        Map.entry("LECT-AY", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-AY-1/9", "Unit 18 Pre-Six Lecturer"),
        Map.entry("LECT-MISCELLANEOUS/PART TIME", "Unit 18 Pre-Six Lecturer"),
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
        Map<Integer, BigDecimal> fteByMonth = new HashMap<>();
        BigDecimal fte = BigDecimal.ZERO;
        BigDecimal totalCompensation = BigDecimal.ZERO;
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal julSepCompensation = BigDecimal.ZERO;
        BigDecimal julSepSalary = BigDecimal.ZERO;
    }

    /* B/E/E salary-scale variants (e.g. PROF-AY-B/E/E) map like their base titles */
    public static String instructorTypeFor(String jobCodeDescription) {
        String normalized = jobCodeDescription.endsWith("-B/E/E")
            ? jobCodeDescription.substring(0, jobCodeDescription.length() - "-B/E/E".length())
            : jobCodeDescription;
        return INSTRUCTOR_TYPE_BY_JOB_CODE_DESCRIPTION.get(normalized);
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
            if (dopeRecord.getFte() != null) {
                personTally.fteByMonth.merge(dopeRecord.getFiscalMonth(), dopeRecord.getFte(), BigDecimal::max);
            }
            if (dopeRecord.getMonetaryAmount() != null) {
                boolean isSalary = "SALARY".equals(dopeRecord.getExpenseType());
                personTally.totalCompensation = personTally.totalCompensation.add(dopeRecord.getMonetaryAmount());
                if (isSalary) {
                    personTally.salary = personTally.salary.add(dopeRecord.getMonetaryAmount());
                }
                if (dopeRecord.getFiscalMonth() <= 3) {
                    personTally.julSepCompensation = personTally.julSepCompensation.add(dopeRecord.getMonetaryAmount());
                    if (isSalary) {
                        personTally.julSepSalary = personTally.julSepSalary.add(dopeRecord.getMonetaryAmount());
                    }
                }
            }
        }

        // roll up people into job code tallies; person FTE = average over months present.
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

                BigDecimal personFte = BigDecimal.ZERO;
                if (!personTally.fteByMonth.isEmpty()) {
                    BigDecimal fteSum = personTally.fteByMonth.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                    personFte = fteSum.divide(
                        new BigDecimal(personTally.fteByMonth.size()), 2, RoundingMode.HALF_UP);
                }

                addPerson(tally, personTally, personFte);

                PersonTally typePerson = peopleByInstructorType
                    .computeIfAbsent(instructorType, k -> new HashMap<>())
                    .computeIfAbsent(personEntry.getKey(), k -> new PersonTally());
                typePerson.fte = typePerson.fte.add(personFte);
                typePerson.totalCompensation = typePerson.totalCompensation.add(personTally.totalCompensation);
                typePerson.salary = typePerson.salary.add(personTally.salary);
                typePerson.julSepCompensation = typePerson.julSepCompensation.add(personTally.julSepCompensation);
                typePerson.julSepSalary = typePerson.julSepSalary.add(personTally.julSepSalary);
            }

            byJobCodeDescription.put(jobCodeEntry.getKey(), tally);
        }

        Map<String, DopeTotals> byInstructorType = new HashMap<>();
        for (Map.Entry<String, Map<String, PersonTally>> typeEntry : peopleByInstructorType.entrySet()) {
            DopeTotals tally = new DopeTotals();
            for (PersonTally person : typeEntry.getValue().values()) {
                addPerson(tally, person, person.fte);
            }
            byInstructorType.put(typeEntry.getKey(), tally);
        }

        return new DopeSummary(byJobCodeDescription, byInstructorType, unmappedJobCodes, employeeIds.size());
    }

    private static void addPerson(DopeTotals tally, PersonTally personTally, BigDecimal personFte) {
        tally.people++;
        tally.fte = tally.fte.add(personFte);
        tally.totalCompensation = tally.totalCompensation.add(personTally.totalCompensation);
        tally.salary = tally.salary.add(personTally.salary);
        tally.julSepCompensation = tally.julSepCompensation.add(personTally.julSepCompensation);
        tally.julSepSalary = tally.julSepSalary.add(personTally.julSepSalary);
    }
}
