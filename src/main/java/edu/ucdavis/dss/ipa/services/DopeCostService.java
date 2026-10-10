package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.StaffingCostSummaryView;
import edu.ucdavis.dss.ipa.repositories.DatamartRepository;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Bridges Banner instructional assignments to UCPath DOPE payroll: given a department-year of DOPE
 * records and the people Banner says taught, produces each person's cost and how confidently it was
 * matched. Keeps the empl-id match vocabulary in one place.
 *
 * Gated with DatamartRepository, so the two are present or absent together.
 */
@Service
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class DopeCostService {
    @Inject DatamartRepository datamartRepository;

    /**
     * One instructor's cost resolution. status is the match outcome ("id" matched in-department,
     * "elsewhere" paid by another department, "no_record" no DOPE row at all, "no_id" no crosswalk
     * empl id); label is the human-readable form. The pay figures are set only when matched
     * in-department — a person funded elsewhere has no cost in this department to report.
     */
    public record PersonCostResult(String status, String label,
                                   Map<String, Set<Integer>> monthsByJobCode,
                                   BigDecimal personSalary, BigDecimal personCost,
                                   Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode) {

        static PersonCostResult matched(Map<String, Set<Integer>> monthsByJobCode,
                                        BigDecimal personSalary, BigDecimal personCost,
                                        Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode) {
            return new PersonCostResult("id", "matched (empl id)", monthsByJobCode, personSalary,
                personCost, fteByMonthByJobCode);
        }

        /**
         * The person's DOPE job code description(s) for this term and these instructor types, so the
         * column agrees with the FTE beside it: a TA row names their TA title, not the Reader appointment
         * they also hold, and a Fall row names only what they were paid as in Oct-Dec.
         *
         * **Term-scoped as of 2026-07-30, and it must stay in step with fteFor.** Before that it listed
         * every matching title in the fiscal year, which put `LECT-AY, LECT-AY-CONTINUING` beside a Fall
         * `Term FTE` of 0.67 — the pre-six appointment had ended in August, so naming it on a Fall course
         * row asserted a title the person did not hold while teaching it.
         *
         * Null when nothing matches — a Banner assignment we cannot corroborate with a payroll
         * appointment of the same kind, which reads as blank rather than as a mismatched title.
         */
        public String jobCodesFor(Set<Integer> fiscalMonths, Set<String> instructorTypes) {
            if (monthsByJobCode == null) {
                return null;
            }
            String joined = String.join(", ",
                relevantJobCodes(codesActiveInTerm(monthsByJobCode, fiscalMonths), instructorTypes));
            return joined.isBlank() ? null : joined;
        }

        /**
         * The job codes with any activity in the term's fiscal months. Applied **before** the role
         * fallback below, not after: a title held only outside this term would otherwise satisfy the
         * role match, suppress the fallback, and leave the cell blank once the month filter ran.
         *
         * Two overloads rather than one generic, because the two maps hold months differently — the job
         * code column needs only presence, the FTE column needs the value too.
         */
        private static Set<String> codesActiveInTerm(Map<String, Set<Integer>> monthsByJobCode,
                                                     Set<Integer> fiscalMonths) {
            return activeInTerm(monthsByJobCode, Set::stream, fiscalMonths);
        }

        private static Set<String> fteCodesActiveInTerm(
                Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode, Set<Integer> fiscalMonths) {
            return activeInTerm(fteByMonthByJobCode, months -> months.keySet().stream(), fiscalMonths);
        }

        private static <V> Set<String> activeInTerm(Map<String, V> byJobCode,
                                                    Function<V, Stream<Integer>> months,
                                                    Set<Integer> fiscalMonths) {
            return byJobCode.entrySet().stream()
                .filter(entry -> months.apply(entry.getValue()).anyMatch(fiscalMonths::contains))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
        }

        /**
         * The job codes a row should read: those matching the row's role, or — when none match — the
         * person's **unmapped** titles instead.
         *
         * The fallback exists because role scoping alone hid real appointments. `ADJ PROF-AY` maps to no
         * instructor type, so an adjunct professor teaching a course showed a blank job code and blank
         * FTE, indistinguishable from having no payroll record at all. Falling back says "nothing matched
         * this role, but here is what they are actually paid as", which is the question the column exists
         * to answer.
         *
         * It is a fallback rather than a widening: a TA holding both a TA and a Reader appointment still
         * reads only the TA title, because the Reader title *is* mapped and so never reaches this branch.
         */
        private static Set<String> relevantJobCodes(Collection<String> jobCodes,
                                                    Set<String> instructorTypes) {
            Set<String> matching = jobCodes.stream()
                .filter(jobCode -> matchesType(jobCode, instructorTypes))
                .collect(Collectors.toCollection(TreeSet::new));
            if (!matching.isEmpty()) {
                return matching;
            }
            return jobCodes.stream()
                .filter(jobCode -> DopeSummaryCalculator.instructorTypeFor(jobCode) == null)
                .collect(Collectors.toCollection(TreeSet::new));
        }

        /**
         * Whether a job code belongs to one of these instructor types. **The null check is required, not
         * defensive:** instructorTypeFor returns null for any unmapped job code — a GSR, a postdoc, a
         * staff title, or one we deliberately unmapped like RECALL FACULTY — and instructorTypes is a
         * Set.of(), which throws NullPointerException on contains(null) rather than returning false.
         */
        private static boolean matchesType(String jobCode, Set<String> instructorTypes) {
            String type = DopeSummaryCalculator.instructorTypeFor(jobCode);
            return type != null && instructorTypes.contains(type);
        }

        static PersonCostResult status(String status, String label) {
            return new PersonCostResult(status, label, null, null, null, null);
        }

        /**
         * The person's appointment level in one term, counting only job codes belonging to the given
         * instructor types — so a TA row reads TA titles and a Reader appointment does not inflate it.
         *
         * A LEVEL, on the same definition By Category uses: monthly FTE is summed across the matching job
         * codes so concurrent titles add up, then the **highest** month of the term is taken. A 50%
         * appointment reads 0.50. Null when nothing matches, which keeps the cell blank rather than
         * showing a misleading zero.
         */
        public BigDecimal fteFor(Set<Integer> fiscalMonths, Set<String> instructorTypes) {
            if (fteByMonthByJobCode == null) {
                return null;
            }
            // same term restriction then role-then-unmapped fallback as the job code column, in the same
            // order, so the two agree on every row
            Set<String> useCodes = relevantJobCodes(
                fteCodesActiveInTerm(fteByMonthByJobCode, fiscalMonths), instructorTypes);

            Map<Integer, BigDecimal> byMonth = new HashMap<>();
            fteByMonthByJobCode.forEach((jobCode, months) -> {
                if (!useCodes.contains(jobCode)) {
                    return;
                }
                months.forEach((month, fte) -> {
                    if (fiscalMonths.contains(month)) {
                        byMonth.merge(month, fte, BigDecimal::add);
                    }
                });
            });
            if (byMonth.isEmpty()) {
                return null;
            }
            return byMonth.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max)
                .setScale(2, RoundingMode.HALF_UP);
        }
    }

    /** DOPE rows for a department-year, or null when the query failed. */
    public List<DopeRecord> getDopeRecords(String departmentCode, int fiscalYear) {
        return datamartRepository.getDopeRecords(departmentCode, fiscalYear);
    }

    /**
     * Resolve each instructor's cost by exact empl-id match to the department's DOPE. Instructors
     * paid outside the department are classified with a dept-agnostic DOPE lookup (funded elsewhere
     * vs no DOPE record at all) but carry NO cost — their pay belongs to another department.
     *
     * @param personIds       Banner person ids to resolve (real instructors, not placeholders)
     * @param emplIdByPerson  person id -> normalized UCPath empl id, where the crosswalk had one
     */
    public Map<String, PersonCostResult> resolveCosts(List<DopeRecord> dopeRecords,
                                                      Set<String> personIds,
                                                      Map<String, String> emplIdByPerson,
                                                      int fiscalYear) {
        Map<String, PersonCost> costByEmplId = buildCostByEmplId(dopeRecords);
        Map<String, PersonCostResult> resultByPerson = new HashMap<>();
        Set<String> unresolvedEmplIds = new HashSet<>();

        for (String personId : personIds) {
            String emplId = emplIdByPerson.get(personId);
            if (emplId == null || emplId.isBlank()) {
                resultByPerson.put(personId, PersonCostResult.status("no_id", "unmatched (no empl id)"));
                continue;
            }
            PersonCost cost = costByEmplId.get(emplId);
            if (cost != null) {
                resultByPerson.put(personId, PersonCostResult.matched(
                    cost.monthsByJobCode, cost.salary, cost.compensation,
                    cost.fteByMonthByJobCode));
            } else {
                unresolvedEmplIds.add(emplId);
            }
        }

        // one dept-agnostic lookup to explain the not-in-department instructors
        Map<String, Set<String>> departmentsByEmployee =
            datamartRepository.getDepartmentsByEmployee(unresolvedEmplIds, fiscalYear);

        for (String personId : personIds) {
            if (resultByPerson.containsKey(personId)) {
                continue;
            }
            String emplId = emplIdByPerson.get(personId);
            Set<String> departments = departmentsByEmployee == null ? null : departmentsByEmployee.get(emplId);
            if (departments != null && !departments.isEmpty()) {
                resultByPerson.put(personId, PersonCostResult.status("elsewhere",
                    "funded elsewhere (" + String.join(", ", new TreeSet<>(departments)) + ")"));
            } else {
                resultByPerson.put(personId, PersonCostResult.status("no_record", "no DOPE record"));
            }
        }

        return resultByPerson;
    }

    /** Departmental totals plus empl-id match coverage, for the Cost Match diagnostics tab. */
    public StaffingCostSummaryView summarize(DopeSummary dopeSummary,
                                             Map<String, PersonCostResult> costByPerson) {
        // department totals via the person-first calculator (accurate FTE, not a raw row sum)
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal fte = BigDecimal.ZERO;
        for (DopeTotals totals : dopeSummary.getByInstructorType().values()) {
            salary = salary.add(totals.getSalary());
            summerSalary = summerSalary.add(totals.getSummerSalary());
            compensation = compensation.add(totals.getTotalCompensation());
            fte = fte.add(totals.getFte());
        }

        BigDecimal attributedSalary = BigDecimal.ZERO;
        BigDecimal attributedCompensation = BigDecimal.ZERO;
        int matchedById = 0, fundedElsewhere = 0, noDopeRecord = 0, noEmplId = 0;
        for (PersonCostResult result : costByPerson.values()) {
            switch (result.status()) {
                case "id":
                    matchedById++;
                    attributedSalary = attributedSalary.add(result.personSalary());
                    attributedCompensation = attributedCompensation.add(result.personCost());
                    break;
                case "elsewhere": fundedElsewhere++; break;
                case "no_record": noDopeRecord++; break;
                default: noEmplId++; break;
            }
        }

        return new StaffingCostSummaryView(attributedSalary, attributedCompensation,
            matchedById, fundedElsewhere, noDopeRecord, noEmplId, salary, summerSalary,
            compensation.subtract(salary), compensation, fte, dopeSummary.getDistinctEmployees());
    }

    /* UCPath emplid and Banner WOBEUCD_EMP_ID compared trimmed (Oracle CHAR columns pad with spaces) */
    public static String normalizeEmplId(String emplId) {
        return emplId == null ? "" : emplId.trim();
    }

    /* per-person DOPE totals keyed by trimmed empl id (money is an exact sum; FTE is computed
       separately by the calculator for the summary). Summer Session pay is kept out of the
       academic-year salary/compensation (to match the Fall/Winter/Spring courses shown and the
       reconciliation report's summer treatment) but tallied into summerSalary as a context figure. */
    private static Map<String, PersonCost> buildCostByEmplId(List<DopeRecord> records) {
        Map<String, PersonCost> byEmplId = new HashMap<>();
        for (DopeRecord record : records) {
            PersonCost cost = byEmplId.computeIfAbsent(normalizeEmplId(record.getEmployeeId()),
                k -> new PersonCost());
            boolean isSummer = isSummerPay(record);
            if (record.getMonetaryAmount() != null) {
                boolean isSalary = "SALARY".equals(record.getExpenseType());
                if (isSummer) {
                    if (isSalary) {
                        cost.summerSalary = cost.summerSalary.add(record.getMonetaryAmount());
                    }
                } else {
                    cost.compensation = cost.compensation.add(record.getMonetaryAmount());
                    if (isSalary) {
                        cost.salary = cost.salary.add(record.getMonetaryAmount());
                    }
                }
            }
            // Keyed on the month the pay was EARNED — DopeSummaryCalculator#earnedFiscalMonth explains why
            // the posting month put a July appointment on a Fall course row at 1.50 FTE. A retro row
            // (earn month != posting month) may not ESTABLISH a month, only net into one. Summer job codes
            // stay out entirely: only academic-year codes describe the match.
            Integer earnedMonth = DopeSummaryCalculator.earnedFiscalMonth(record);
            boolean retro = earnedMonth != null && earnedMonth.intValue() != record.getFiscalMonth();
            String jobCode = record.getJobCodeDescription() != null
                ? record.getJobCodeDescription() : "(none)";
            String position = record.getPositionNumber() != null ? record.getPositionNumber() : "(none)";

            if (!isSummer && earnedMonth != null && !retro) {
                if (record.getJobCodeDescription() != null) {
                    cost.monthCandidates
                        .computeIfAbsent(record.getJobCodeDescription(), k -> new HashMap<>())
                        .computeIfAbsent(earnedMonth, k -> new HashSet<>())
                        .add(position);
                }
                if (record.getFte() != null) {
                    cost.fteCandidates
                        .computeIfAbsent(jobCode, k -> new HashMap<>())
                        .computeIfAbsent(earnedMonth, k -> new HashMap<>())
                        .merge(position, record.getFte(), BigDecimal::max);
                }
            }
            // every row nets, retro included — that is how a reversal cancels the month it reverses
            if (!isSummer && earnedMonth != null && record.getMonetaryAmount() != null) {
                cost.net
                    .computeIfAbsent(jobCode, k -> new HashMap<>())
                    .computeIfAbsent(earnedMonth, k -> new HashMap<>())
                    .merge(position, record.getMonetaryAmount(), BigDecimal::add);
            }
        }
        // every row seen, so reversals can now be netted and the position key collapsed away
        byEmplId.values().forEach(PersonCost::settle);
        return byEmplId;
    }

    /* Summer Session pay, excluded from the course-attributed cost (the view shows only
       Fall/Winter/Spring courses). Shares DopeSummaryCalculator's rule with the By Category view, so
       the two views' salary figures rest on the same definition of the academic year. */
    private static boolean isSummerPay(DopeRecord record) {
        return DopeSummaryCalculator.isSummerPay(record.getJobCodeDescription(), record.getFiscalMonth());
    }

    /* per-person DOPE totals; salary/compensation are academic-year (summer excluded), summerSalary
       is the excluded Jul-Sep / Summer Session salary kept for context */
    private static class PersonCost {
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        /* Staging, collapsed by settle() once every row has been seen. Position is a key only so a
           reversal can cancel the appointment it reverses; it never reaches PersonCostResult. */
        final Map<String, Map<Integer, Set<String>>> monthCandidates = new HashMap<>();
        final Map<String, Map<Integer, Map<String, BigDecimal>>> fteCandidates = new HashMap<>();
        final Map<String, Map<Integer, Map<String, BigDecimal>>> net = new HashMap<>();

        /* job code description -> the fiscal months it was live in. Presence only; the DOPE Job Code
           column needs to know which titles were held in a term, not what they paid. Includes titles
           carrying no FTE, which is why it is separate from fteByMonthByJobCode rather than derived from
           its key set — an adjunct with pay but no FTE still belongs in the column. */
        final Map<String, Set<Integer>> monthsByJobCode = new HashMap<>();
        /* job code description -> fiscal month -> FTE, summed across positions after reversed
           position-months are dropped. Kept as monthly detail rather than one number, because By Course
           slices it per term and per role — see PersonCostResult#fteFor. Summer months never enter. */
        final Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode = new HashMap<>();

        /**
         * Drop every position-month whose pay nets to zero or less, then collapse positions away. A
         * reversed appointment was never held: English FY2026 cancelled a 0.86 `LECT-AY-1/9` in December
         * and restated it as a 0.57 `LECT-AY` on a new position, and counting both read 1.43 FTE.
         *
         * A position-month with no monetary row at all is kept — absent money is not a reversal.
         */
        void settle() {
            fteCandidates.forEach((jobCode, byMonth) -> byMonth.forEach((month, byPosition) -> {
                BigDecimal fte = BigDecimal.ZERO;
                for (Map.Entry<String, BigDecimal> entry : byPosition.entrySet()) {
                    if (live(jobCode, month, entry.getKey())) {
                        fte = fte.add(entry.getValue());
                    }
                }
                if (fte.signum() > 0) {
                    fteByMonthByJobCode.computeIfAbsent(jobCode, k -> new HashMap<>()).put(month, fte);
                }
            }));
            monthCandidates.forEach((jobCode, byMonth) -> byMonth.forEach((month, positions) -> {
                if (positions.stream().anyMatch(position -> live(jobCode, month, position))) {
                    monthsByJobCode.computeIfAbsent(jobCode, k -> new HashSet<>()).add(month);
                }
            }));
        }

        private boolean live(String jobCode, Integer month, String position) {
            BigDecimal amount = net.getOrDefault(jobCode, Map.of())
                .getOrDefault(month, Map.of()).get(position);
            return amount == null || amount.signum() > 0;
        }
    }
}
