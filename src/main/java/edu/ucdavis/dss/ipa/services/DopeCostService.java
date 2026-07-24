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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "DATAMART_URL")
public class DopeCostService {
    @Inject DatamartRepository datamartRepository;

    /**
     * One instructor's cost resolution. status is the match outcome ("id" matched in-department,
     * "elsewhere" paid by another department, "no_record" no DOPE row at all, "no_id" no crosswalk
     * empl id); label is the human-readable form. The pay figures are set only when matched
     * in-department — a person funded elsewhere has no cost in this department to report.
     */
    public record PersonCostResult(String status, String label, String jobCode,
                                   BigDecimal personSalary, BigDecimal personCost,
                                   BigDecimal allocatedSalary, BigDecimal allocatedCost,
                                   BigDecimal summerSalary) {

        static PersonCostResult matched(String jobCode, BigDecimal personSalary, BigDecimal personCost,
                                        BigDecimal allocatedSalary, BigDecimal allocatedCost,
                                        BigDecimal summerSalary) {
            return new PersonCostResult("id", "matched (empl id)", jobCode, personSalary, personCost,
                allocatedSalary, allocatedCost, summerSalary);
        }

        static PersonCostResult status(String status, String label) {
            return new PersonCostResult(status, label, null, null, null, null, null, null);
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
     * @param coursesByPerson person id -> distinct courses taught, the even-split denominator
     */
    public Map<String, PersonCostResult> resolveCosts(List<DopeRecord> dopeRecords,
                                                      Set<String> personIds,
                                                      Map<String, String> emplIdByPerson,
                                                      Map<String, Set<String>> coursesByPerson,
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
                int courseCount = Math.max(1, coursesByPerson.getOrDefault(personId, Set.of()).size());
                BigDecimal divisor = new BigDecimal(courseCount);
                resultByPerson.put(personId, PersonCostResult.matched(
                    String.join(", ", cost.jobCodeDescriptions), cost.salary, cost.compensation,
                    cost.salary.divide(divisor, 2, RoundingMode.HALF_UP),
                    cost.compensation.divide(divisor, 2, RoundingMode.HALF_UP),
                    cost.summerSalary));
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
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal fte = BigDecimal.ZERO;
        for (DopeTotals totals : dopeSummary.getByInstructorType().values()) {
            salary = salary.add(totals.getSalary());
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
            matchedById, fundedElsewhere, noDopeRecord, noEmplId, salary, compensation.subtract(salary),
            compensation, fte, dopeSummary.getDistinctEmployees());
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
            // summer job codes stay out of the DOPE Job Code column — only AY codes describe the match
            if (!isSummer && record.getJobCodeDescription() != null) {
                cost.jobCodeDescriptions.add(record.getJobCodeDescription());
            }
        }
        return byEmplId;
    }

    /* Summer Session pay, excluded from the course-attributed cost (the view shows only
       Fall/Winter/Spring courses). NOTE: this is a stricter rule than the By Category view's, which
       relies on DopeSummaryCalculator and does NOT drop explicit Summer Session job codes — so the
       two views' salary figures do not tie. Unify before this leaves the prototype.
       (a) explicit Summer Session job codes (e.g. LECT IN SUMMER SESSION) are dropped outright, and
       (b) for the summer-bearing student categories (TA/AI/Reader) Jul-Sep pay is Summer Session.
       12-month faculty/lecturer pay is kept whole (their salary is spread across summer months). */
    private static boolean isSummerPay(DopeRecord record) {
        String jobCode = record.getJobCodeDescription();
        if (jobCode != null && jobCode.toUpperCase().contains("SUMMER")) {
            return true;
        }
        return record.getFiscalMonth() <= 3 && DopeSummaryCalculator.isSummerSessionBearing(jobCode);
    }

    /* per-person DOPE totals; salary/compensation are academic-year (summer excluded), summerSalary
       is the excluded Jul-Sep / Summer Session salary kept for context */
    private static class PersonCost {
        BigDecimal salary = BigDecimal.ZERO;
        BigDecimal compensation = BigDecimal.ZERO;
        BigDecimal summerSalary = BigDecimal.ZERO;
        final Set<String> jobCodeDescriptions = new TreeSet<>();
    }
}
