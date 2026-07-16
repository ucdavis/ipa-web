package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.datamart.DopeSummary;
import edu.ucdavis.dss.datamart.DopeTotals;
import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationCategoryView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.repositories.DatamartRepository;
import edu.ucdavis.dss.ipa.services.BudgetCalculationService;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.ExpenseItemService;
import edu.ucdavis.dss.ipa.services.TermService;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class JpaBudgetReconciliationReportService implements BudgetReconciliationReportService {
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject TermService termService;
    @Inject ExpenseItemService expenseItemService;
    @Inject BudgetCalculationService budgetCalculationService;
    @Inject DatamartRepository datamartRepository;

    /* categories whose regular-year pay starts in October, so Jul-Sep actuals are Summer Session */
    private static final Set<String> SUMMER_SESSION_BEARING_TYPES =
        Set.of("TAs", "Associate Instructor", "Readers");

    /* Ladder Faculty is state-funded ($0 in the SIB budget) — context row, not a variance */
    private static final Set<String> CONTEXT_ONLY_TYPES =
        Set.of("Ladder Faculty", DopeSummaryCalculator.UNMAPPED);

    private static final Map<String, BudgetSummary[]> BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE = buildBudgetSummaryMap();

    private static Map<String, BudgetSummary[]> buildBudgetSummaryMap() {
        Map<String, BudgetSummary[]> map = new LinkedHashMap<>();
        map.put("Ladder Faculty", new BudgetSummary[] {BudgetSummary.LADDER_FACULTY_COST, BudgetSummary.LADDER_FACULTY_COUNT});
        map.put("New Faculty Hire", new BudgetSummary[] {BudgetSummary.NEW_FACULTY_HIRE_COST, BudgetSummary.NEW_FACULTY_HIRE_COUNT});
        map.put("Lecturer SOE", new BudgetSummary[] {BudgetSummary.LECTURER_SOE_COST, BudgetSummary.LECTURER_SOE_COUNT});
        map.put("Continuing Lecturer", new BudgetSummary[] {BudgetSummary.CONTINUING_LECTURER_COST, BudgetSummary.CONTINUING_LECTURER_COUNT});
        map.put("Emeriti - Recalled", new BudgetSummary[] {BudgetSummary.EMERITI_COST, BudgetSummary.EMERITI_COUNT});
        map.put("Visiting Professor", new BudgetSummary[] {BudgetSummary.VISITING_PROFESSOR_COST, BudgetSummary.VISITING_PROFESSOR_COUNT});
        map.put("Unit 18 Pre-Six Lecturer", new BudgetSummary[] {BudgetSummary.UNIT18_LECTURER_COST, BudgetSummary.UNIT18_LECTURER_COUNT});
        map.put("Continuing Lecturer - Augmentation", new BudgetSummary[] {BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COST, BudgetSummary.CONTINUING_LECTURER_AUGMENTATION_COUNT});
        map.put("Associate Instructor", new BudgetSummary[] {BudgetSummary.ASSOCIATE_INSTRUCTOR_COST, BudgetSummary.ASSOCIATE_INSTRUCTOR_COUNT});
        map.put("Instructor", new BudgetSummary[] {BudgetSummary.INSTRUCTOR_COST, BudgetSummary.INSTRUCTOR_COUNT});
        map.put("TAs", new BudgetSummary[] {BudgetSummary.TA_COST, BudgetSummary.TA_COUNT});
        map.put("Readers", new BudgetSummary[] {BudgetSummary.READER_COST, BudgetSummary.READER_COUNT});
        return map;
    }

    @Override
    public BudgetReconciliationReportView generate(long workgroupId, long year, String departmentCode) {
        int fiscalYear = (int) year + 1;

        // planned side: same inputs and calculation the budget comparison Excel export uses
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        BudgetScenario budgetScenario = selectScenario(workgroupId, year);
        Workgroup workgroup = budget.getSchedule().getWorkgroup();

        List<String> termCodes = new ArrayList<>();
        for (String termCodeShort : Arrays.asList(TermDescription.FALL.getShortTermCode(),
            TermDescription.WINTER.getShortTermCode(), TermDescription.SPRING.getShortTermCode())) {
            termCodes.add(termService.getTermCodeFromYearAndTerm(budget.getSchedule().getYear(), termCodeShort));
        }

        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts().stream()
            .filter(sgc -> (sgc.isDisabled() == false && termCodes.contains(sgc.getTermCode())))
            .collect(Collectors.toList());
        List<LineItem> lineItems = budgetScenario.getLineItems().stream()
            .filter(li -> li.getHidden() == false).collect(Collectors.toList());
        List<ExpenseItem> expenseItems = expenseItemService.findByBudgetScenarioId(budgetScenario.getId());

        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = budgetCalculationService.calculateTermTotals(
            budget, budgetScenario, sectionGroupCosts, termCodes, workgroup, lineItems, expenseItems);
        Map<BudgetSummary, BigDecimal> planned = termTotals.get("combined");

        // actuals side: Datamart DOPE tallies for the matching fiscal year
        List<DopeRecord> dopeRecords = datamartRepository.getDopeRecords(departmentCode, fiscalYear);
        if (dopeRecords == null) {
            throw new IllegalStateException("Datamart query failed for department " + departmentCode);
        }
        DopeSummary tallies = DopeSummaryCalculator.calculate(dopeRecords);

        List<BudgetReconciliationCategoryView> categories = new ArrayList<>();
        for (Map.Entry<String, BudgetSummary[]> entry : BUDGET_SUMMARY_BY_INSTRUCTOR_TYPE.entrySet()) {
            categories.add(buildCategory(entry.getKey(), planned.get(entry.getValue()[0]),
                planned.get(entry.getValue()[1]), tallies.getByInstructorType().get(entry.getKey())));
        }
        categories.add(buildCategory(DopeSummaryCalculator.UNMAPPED, null, null,
            tallies.getByInstructorType().get(DopeSummaryCalculator.UNMAPPED)));

        return new BudgetReconciliationReportView(workgroupId, year, fiscalYear, departmentCode,
            budgetScenario.getName(), categories);
    }

    /* the approved budget request, falling back to the most recent scenario */
    private BudgetScenario selectScenario(long workgroupId, long year) {
        List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);

        return budgetScenarios.stream().filter(BudgetScenario::getIsBudgetRequest)
            .max(Comparator.comparing(BudgetScenario::getCreationDate))
            .orElseGet(() -> budgetScenarios.stream()
                .max(Comparator.comparing(BudgetScenario::getCreationDate))
                .orElseThrow(() -> new IllegalStateException(
                    "No budget scenario found for workgroup " + workgroupId + " year " + year)));
    }

    private BudgetReconciliationCategoryView buildCategory(String instructorType, BigDecimal plannedCost,
                                                           BigDecimal plannedCount, DopeTotals tally) {
        boolean includedInComparison = !CONTEXT_ONLY_TYPES.contains(instructorType);

        if (tally == null) {
            tally = new DopeTotals();
        }

        BigDecimal comparableSalary = SUMMER_SESSION_BEARING_TYPES.contains(instructorType)
            ? tally.getAcademicYearSalary() : tally.getSalary();
        BigDecimal variance = includedInComparison && plannedCost != null
            ? comparableSalary.subtract(plannedCost) : null;

        return new BudgetReconciliationCategoryView(instructorType, includedInComparison,
            plannedCost, plannedCount, tally.getPeople(), tally.getFte(), tally.getTotalCompensation(),
            tally.getSalary(), tally.getJulSepCompensation(), comparableSalary, variance);
    }
}
