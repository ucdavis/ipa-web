package edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationExcelView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class StaffingByCourseExcelView extends AbstractXlsxView {
    private final StaffingByCourseReportView reportView;
    private final BudgetReconciliationReportView reconciliationView;

    public StaffingByCourseExcelView(StaffingByCourseReportView reportView,
                                     BudgetReconciliationReportView reconciliationView) {
        this.reportView = reportView;
        this.reconciliationView = reconciliationView;
    }

    private static final int LAST_COLUMN = 13;

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Disposition", String.format(
            "attachment; filename=\"Staffing-By-Course-wg%d-%d.xlsx\"",
            reportView.getWorkgroupId(), reportView.getYear()));

        // summary tab = the Budget Reconciliation report (present only when a department was given)
        if (reconciliationView != null) {
            BudgetReconciliationExcelView.writeSheet(workbook, reconciliationView, "By Category");
        }
        buildByCourseSheet(workbook.createSheet("By Course"));
        // empl-id match diagnostics kept as a working tab while prototyping the cost bridge
        StaffingCostSummaryView summary = reportView.getCostSummary();
        if (summary != null) {
            buildCostMatchSheet(workbook.createSheet("Cost Match"), summary);
        }
        ExcelHelper.expandHeaders(workbook);
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

    /* planned (IPA) vs actual (Banner) staffing per course, one collapsible outline group per course */
    private void buildByCourseSheet(Sheet sheet) {
        writeTitle(sheet, "Staffing by course", LAST_COLUMN);

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
        for (int column = 0; column <= LAST_COLUMN; column++) {
            sheet.autoSizeColumn(column);
        }
    }

    /* scenario title merged across the data columns; autoSizeColumn ignores merged cells, so the
       long title doesn't stretch column A */
    private void writeTitle(Sheet sheet, String prefix, int lastColumn) {
        Row titleRow = sheet.createRow(0);
        titleRow.createCell(0).setCellValue(prefix + ": " + reportView.getBudgetScenarioName()
            + " (" + reportView.getYear() + "-" + (reportView.getYear() + 1) + ")");
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, lastColumn));
    }
}
