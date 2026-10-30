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
 * records and the people Banner says taught, produces each person's cost and how it was matched.
 */
@Service
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class DopeCostService {
    @Inject DatamartRepository datamartRepository;

    /**
     * One instructor's cost resolution. status is the match outcome ("id" matched in-department,
     * "elsewhere" paid by another department, "no_record" no DOPE row at all, "no_id" no crosswalk
     * empl id). The pay figures are set only when matched in-department; a person paid elsewhere has no
     * cost in this department to report.
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
         * The person's DOPE job code descriptions for this term and these instructor types: a TA row
         * names their TA title, not the Reader appointment they also hold, and a Fall row names only what
         * they were paid as in Oct-Dec. Must stay in step with fteFor so the column agrees with the FTE
         * beside it. Null when nothing matches.
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
         * The job codes with any activity in the term's fiscal months. Applied before the role fallback
         * below, not after: a title held only outside this term would otherwise satisfy the role match,
         * suppress the fallback, and leave the cell blank once the month filter ran.
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
         * The job codes a row should read: those matching the row's role, or, when none match, the
         * person's unmapped titles instead. Without the fallback, an adjunct professor (ADJ PROF-AY maps
         * to no instructor type) would read as if they had no payroll record at all.
         *
         * It is a fallback rather than a widening: a TA holding both a TA and a Reader appointment still
         * reads only the TA title, because the Reader title is mapped and so never reaches this branch.
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

        /* The null check is required, not defensive: instructorTypeFor returns null for any unmapped job
           code, and Set.of() throws NullPointerException on contains(null). */
        private static boolean matchesType(String jobCode, Set<String> instructorTypes) {
            String type = DopeSummaryCalculator.instructorTypeFor(jobCode);
            return type != null && instructorTypes.contains(type);
        }

        static PersonCostResult status(String status, String label) {
            return new PersonCostResult(status, label, null, null, null, null);
        }

        /**
         * The person's appointment level in one term, counting only job codes belonging to the given
         * instructor types. A level, on the same definition By Category uses: monthly FTE is summed across
         * the matching job codes, then the highest month of the term is taken. Null when nothing matches,
         * which keeps the cell blank rather than showing a misleading zero.
         */
        public BigDecimal fteFor(Set<Integer> fiscalMonths, Set<String> instructorTypes) {
            if (fteByMonthByJobCode == null) {
                return null;
            }
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
     * paid outside the department are classified with a lookup across all departments but carry no
     * cost; their pay belongs to another department.
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

        Map<String, Set<String>> departmentsByEmployee =
            datamartRepository.getDepartmentsByEmployee(unresolvedEmplIds, fiscalYear);
        if (departmentsByEmployee == null) {
            throw new IllegalStateException("Datamart departments-by-employee query failed for FY" + fiscalYear);
        }

        for (String personId : personIds) {
            if (resultByPerson.containsKey(personId)) {
                continue;
            }
            String emplId = emplIdByPerson.get(personId);
            Set<String> departments = departmentsByEmployee.get(emplId);
            if (departments != null && !departments.isEmpty()) {
                resultByPerson.put(personId, PersonCostResult.status("elsewhere",
                    "funded elsewhere (" + String.join(", ", new TreeSet<>(departments)) + ")"));
            } else {
                resultByPerson.put(personId, PersonCostResult.status("no_record", "no DOPE record"));
            }
        }

        return resultByPerson;
    }

    public StaffingCostSummaryView summarize(DopeSummary dopeSummary,
                                             Map<String, PersonCostResult> costByPerson) {
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
            // Keyed on the month the pay was earned (see DopeSummaryCalculator#earnedFiscalMonth). A retro
            // row may not establish a month, only net into one.
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
        byEmplId.values().forEach(PersonCost::settle);
        return byEmplId;
    }

    /* the same summer rule as By Category, so both views share one definition of the academic year */
    private static boolean isSummerPay(DopeRecord record) {
        return DopeSummaryCalculator.isSummerPay(record.getJobCodeDescription(), record.getFiscalMonth());
    }

    private static class PersonCost {
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        /* keyed by position only so a reversal can cancel the appointment it reverses */
        final Map<String, Map<Integer, Set<String>>> monthCandidates = new HashMap<>();
        final Map<String, Map<Integer, Map<String, BigDecimal>>> fteCandidates = new HashMap<>();
        final Map<String, Map<Integer, Map<String, BigDecimal>>> net = new HashMap<>();

        /* separate from fteByMonthByJobCode because it includes titles carrying no FTE: an adjunct with
           pay but no FTE still belongs in the job code column */
        final Map<String, Set<Integer>> monthsByJobCode = new HashMap<>();
        final Map<String, Map<Integer, BigDecimal>> fteByMonthByJobCode = new HashMap<>();

        /**
         * Drop every position-month whose pay nets to zero or less, then collapse positions away: a
         * reversed appointment was never held. A position-month with no monetary row at all is kept,
         * because absent money is not a reversal.
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
