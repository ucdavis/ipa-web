package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

/**
 * The reconciliation workbook: By Category, By Course, and the Cost Match diagnostics tab. Tabs are
 * present only when their data source was configured (see BudgetReconciliationReportView).
 */
public class BudgetReconciliationExcelView extends AbstractXlsxView {
    private final BudgetReconciliationReportView reportView;

    public BudgetReconciliationExcelView(BudgetReconciliationReportView reportView) {
        this.reportView = reportView;
    }

    private static final int BY_COURSE_LAST_COLUMN = 13;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Disposition", String.format(
            "attachment; filename=\"Budget-Reconciliation-%s-FY%d.xlsx\"",
            reportView.getWorkgroupCode(), reportView.getFiscalYear()));

        buildByCategorySheet(workbook.createSheet("By Category"));
        buildByCourseSheet(workbook.createSheet("By Course"));
        // empl-id match diagnostics kept as a working tab while prototyping the cost bridge
        if (reportView.getCostSummary() != null) {
            buildCostMatchSheet(workbook.createSheet("Cost Match"), reportView.getCostSummary());
        }
        ExcelHelper.expandHeaders(workbook);
    }

    /* planned vs actual cost per instructor type, scoped to the payroll department */
    private void buildByCategorySheet(Sheet sheet) {
        // Headers must be the sheet's first physical row: expandHeaders only autosizes columns that
        // have a cell in that row, so anything narrower leaves later columns at the default width.
        // Nothing may be written above them either — setSheetHeader createRow(0)s and would overwrite
        // it. Columns name their source — IPA (the budget scenario), Payroll (UCPath DOPE) and Banner
        // — matching the Source column on the By Course tab. IPA Count is the plan's count field:
        // course assignments for the faculty and lecturer categories, but TAs' and Readers' own units,
        // which is why it isn't called Courses.
        ExcelHelper.setSheetHeader(sheet, Arrays.asList(
            "Instructor Type", "IPA Cost", "IPA Count", "Payroll Individuals",
            "Banner TA Assignments", "Banner TA Individuals", "Payroll FTE",
            "Actual Total Compensation", "Actual Salary", "Actual Fringe", "Jul-Sep Salary",
            "Academic Year Salary", "Variance", "% Variance"));

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

            // context rows carry no variance, so they say so in the label rather than in a Notes
            // column that would be blank on every other row
            ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                category.isIncludedInComparison() ? category.getInstructorType()
                    : category.getInstructorType() + " (context only)",
                category.getPlannedCost(),
                category.getPlannedCount(),
                category.getActualPeople(),
                category.getBannerTaAssignments(),
                category.getBannerTaIndividuals(),
                category.getActualFte(),
                category.getActualTotalCompensation(),
                category.getActualSalary(),
                category.getActualTotalCompensation().subtract(category.getActualSalary()),
                // salary-only, so it's in the same units as the column before and after it: where a
                // category is summer-bearing, Actual Salary - Jul-Sep Salary = Comparable Salary
                category.getActualJulSepSalary(),
                category.getComparableSalary(),
                category.getVariance(),
                percentVariance(category.getVariance(), category.getPlannedCost())));
        }

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
            "Total (compared categories)", totalPlannedCost,
            null, null, null, null, null, null, null, null, null,
            totalComparableSalary, totalVariance, percentVariance(totalVariance, totalPlannedCost)));
    }

    /* planned (IPA) vs actual (Banner) staffing per course, one collapsible outline group per course */
    private void buildByCourseSheet(Sheet sheet) {
        writeTitle(sheet, "Staffing by course", BY_COURSE_LAST_COLUMN);

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
            "Term", "Subject", "Course", "Source", "Name", "Role", "DOPE Job Code", "Sections",
            "Salary (total)", "Total Comp", "Salary (split)", "Total Comp (split)",
            "Summer Salary (excl)", "Cost Match"));

        // one course-header row (identity shown once) followed by its people rows, grouped into a
        // collapsible outline so you can fold each course down to a single line
        for (CourseStaffingView course : reportView.getCourses()) {
            ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                course.getTermCode(), course.getSubjectCode(), course.getCourseNumber(),
                "", "", "", "", ""));

            int firstPersonRow = sheet.getLastRowNum() + 1;

            for (CourseStaffingPersonView planned : course.getPlanned()) {
                ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                    "", "", "", "IPA", planned.getName(), planned.getRole(), "", "",
                    "", "", "", "", "", ""));
            }
            for (CourseStaffingPersonView actual : course.getActual()) {
                ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                    "", "", "", "Banner", actual.getName(), actual.getRole(),
                    actual.getDopeJobCode(), actual.getSections(),
                    actual.getPersonSalary(), actual.getPersonCost(),
                    actual.getAllocatedSalary(), actual.getAllocatedCost(),
                    actual.getSummerSalary(), actual.getCostMatch()));
            }

            int lastPersonRow = sheet.getLastRowNum();
            if (lastPersonRow >= firstPersonRow) {
                sheet.groupRow(firstPersonRow, lastPersonRow);
            }
        }

        // autosize per column (default ignores the merged title, so column A stays narrow)
        for (int column = 0; column <= BY_COURSE_LAST_COLUMN; column++) {
            sheet.autoSizeColumn(column);
        }
    }

    /* departmental cost totals and empl-id match counts as a labeled Metric/Value table */
    private void buildCostMatchSheet(Sheet sheet, StaffingCostSummaryView summary) {
        writeTitle(sheet, "Cost match diagnostics", 1);

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Department payroll (all job codes)", ""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Salary", summary.getDepartmentSalary()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Benefits", summary.getDepartmentBenefits()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Total comp", summary.getDepartmentCompensation()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("FTE", summary.getDepartmentFte()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("People", summary.getDepartmentPeople()));

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("", ""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Course-attributed (matched by empl id)", ""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Salary", summary.getAttributedSalary()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Total comp", summary.getAttributedCompensation()));

        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("", ""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Instructor match (distinct people)", ""));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Matched (empl id)", summary.getMatchedById()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("Funded elsewhere", summary.getFundedElsewhere()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("No DOPE record", summary.getNoDopeRecord()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("No empl id", summary.getNoEmplId()));

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /* scenario title merged across the data columns; autoSizeColumn ignores merged cells, so the
       long title doesn't stretch column A */
    private void writeTitle(Sheet sheet, String prefix, int lastColumn) {
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue(prefix + ": " + reportView.getBudgetScenarioName()
            + " (" + reportView.getYear() + "-" + (reportView.getYear() + 1) + ")");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumn));
    }

    private static String percentVariance(BigDecimal variance, BigDecimal plannedCost) {
        if (variance == null || plannedCost == null || plannedCost.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }

        return variance.multiply(new BigDecimal("100"))
            .divide(plannedCost, 1, RoundingMode.HALF_UP) + "%";
    }
}
