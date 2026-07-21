package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class BudgetReconciliationExcelView extends AbstractXlsxView {
    private final BudgetReconciliationReportView reportView;

    public BudgetReconciliationExcelView(BudgetReconciliationReportView reportView) {
        this.reportView = reportView;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Disposition", String.format(
            "attachment; filename=\"Budget-Reconciliation-%s-FY%d.xlsx\"",
            reportView.getDepartmentCode(), reportView.getFiscalYear()));

        Sheet sheet = workbook.createSheet("Budget Reconciliation");

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
            "Budget: " + reportView.getBudgetScenarioName() + " (" + reportView.getYear() + "-"
                + (reportView.getYear() + 1) + ")",
            "Actuals: UCPath fiscal year " + reportView.getFiscalYear()
                + ", department " + reportView.getDepartmentCode()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(""));

        // setSheetHeader writes the column headers at row 0 so expandHeaders autosizes columns off
        // these short labels, not the long provenance line above (which lands at row 1).
        // Planned Courses counts course assignments; Planned People counts distinct named
        // instructors (one person can teach several courses) and is comparable to Actual People
        ExcelHelper.setSheetHeader(sheet, Arrays.asList(
            "Instructor Type", "Planned Cost", "Planned Courses", "Planned People",
            "Planned Placeholders", "Actual People", "Banner TA Assignments", "Banner TA Individuals",
            "Actual FTE",
            "Actual Total Compensation", "Actual Salary", "Actual Fringe", "Paid Jul-Sep",
            "Comparable Salary", "Variance", "% Variance", "Notes"));

        BigDecimal totalPlannedCost = BigDecimal.ZERO;
        BigDecimal totalComparableSalary = BigDecimal.ZERO;
        BigDecimal totalVariance = BigDecimal.ZERO;

        for (BudgetReconciliationCategoryView category : reportView.getCategories()) {
            if (category.isIncludedInComparison()) {
                totalPlannedCost = totalPlannedCost.add(category.getPlannedCost() != null
                    ? category.getPlannedCost() : BigDecimal.ZERO);
                totalComparableSalary = totalComparableSalary.add(category.getComparableSalary());
                totalVariance = totalVariance.add(category.getVariance() != null
                    ? category.getVariance() : BigDecimal.ZERO);
            }

            ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                category.getInstructorType(),
                category.getPlannedCost(),
                category.getPlannedCount(),
                category.getPlannedPeople(),
                category.getPlannedPlaceholders(),
                category.getActualPeople(),
                category.getBannerTaAssignments(),
                category.getBannerTaIndividuals(),
                category.getActualFte(),
                category.getActualTotalCompensation(),
                category.getActualSalary(),
                category.getActualTotalCompensation().subtract(category.getActualSalary()),
                category.getActualJulSepCompensation(),
                category.getComparableSalary(),
                category.getVariance(),
                percentVariance(category.getVariance(), category.getPlannedCost()),
                category.isIncludedInComparison() ? "" : "Context only - not compared"));
        }

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
            "Total (compared categories)", totalPlannedCost, null, null, null, null, null, null, null, null, null, null, null,
            totalComparableSalary, totalVariance, percentVariance(totalVariance, totalPlannedCost), ""));

        ExcelHelper.expandHeaders(workbook);
    }

    private String percentVariance(BigDecimal variance, BigDecimal plannedCost) {
        if (variance == null || plannedCost == null || plannedCost.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }

        return variance.multiply(new BigDecimal("100"))
            .divide(plannedCost, 1, RoundingMode.HALF_UP) + "%";
    }
}
