package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import edu.ucdavis.dss.datamart.DopeSummaryCalculator;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Builds the reconciliation workbook: two data tables and a compact data dictionary. By Category
 * answers what each instructor category cost against plan; By Course answers who taught what and at
 * what cost.
 *
 * Diagnostics stay out of the workbook by decision (2026-07-28) — the cost-match counts, department
 * payroll totals, Banner TA counts and the plan tie-out are all still computed and served on the JSON
 * endpoint, but a reviewer reading the spreadsheet gets the comparison, not the plumbing behind it.
 *
 * Deliberately free of the servlet API, so it can be unit-tested without a web request and reused outside
 * one. `BudgetReconciliationExcelView` is the only caller today — it adds the Content-Disposition header
 * and delegates.
 */
public class BudgetReconciliationWorkbook {
    private static final List<List<String>> BY_CATEGORY_DATA_DICTIONARY = List.of(
        List.of("Instructor Type",
            "IPA instructor category. UCPath DOPE rows are tallied through job code mappings."),
        List.of("IPA Cost",
            "Budget scenario planned cost for the category."),
        List.of("IPA Count",
            "Course assignments for faculty and lecturer categories; TAs and Readers are counted in "
                + "their own units, which vary by department."),
        List.of("Payroll Individuals",
            "Distinct people with UCPath pay in the category during Oct-Jun, scoped to the payroll "
                + "department code."),
        List.of("FTE Appointments",
            "Sum of each individual's highest appointment level in Oct-Jun. A level, not a total: "
                + "half time reads 0.5 whether it ran one quarter or three."),
        List.of("Academic Year Salary",
            "UCPath salary expense less Summer Session pay, excluding fringe records."),
        List.of("Difference",
            "Academic Year Salary minus IPA Cost."),
        List.of("% Difference",
            "Difference divided by IPA Cost."));

    private final BudgetReconciliationReportView reportView;

    public BudgetReconciliationWorkbook(BudgetReconciliationReportView reportView) {
        this.reportView = reportView;
    }

    static final int BY_COURSE_LAST_COLUMN = 9;
    static final int BY_CATEGORY_LAST_COLUMN = 7;

    /** The suggested file name, used by both the download header and the batch task. */
    public String fileName() {
        return String.format("Budget-Reconciliation-%s-FY%d.xlsx",
            reportView.getWorkgroupCode(), reportView.getFiscalYear());
    }

    /** A new workbook. Caller closes it. */
    public Workbook build() {
        return writeInto(new XSSFWorkbook());
    }

    /** Fills an existing workbook — the download path, where Spring supplies one. */
    public Workbook writeInto(Workbook workbook) {
        buildByCategorySheet(workbook.createSheet("By Category"));
        buildByCourseSheet(workbook.createSheet("By Course"));
        ExcelHelper.expandHeaders(workbook);
        return workbook;
    }

    /* planned vs actual cost per instructor type, scoped to the payroll department */
    void buildByCategorySheet(Sheet sheet) {
        // Headers must be the sheet's first physical row: expandHeaders only autosizes columns that
        // have a cell in that row, so anything narrower leaves later columns at the default width.
        // Nothing may be written above them either — setSheetHeader createRow(0)s and would overwrite
        // it. Columns name their source — IPA (the budget scenario), Payroll (UCPath DOPE) and Banner
        // — matching the Source column on the By Course tab. IPA Count is the plan's count field:
        // course assignments for the faculty and lecturer categories, but TAs' and Readers' own units,
        // which is why it isn't called Courses.
        ExcelHelper.setSheetHeader(sheet, Arrays.asList(
            "Instructor Type", "IPA Cost", "IPA Count", "Payroll Individuals", "FTE Appointments",
            "Academic Year Salary", "Difference", "% Difference"));

        for (BudgetReconciliationCategoryView category : reportView.getCategories()) {
            // Unmapped keeps the suffix saying why its difference is blank; Ladder Faculty carries none
            // by decision. With no totals row there is no other cue that either is excluded from the
            // comparison, so summing Academic Year Salary down the column overstates it.
            ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                DopeSummaryCalculator.UNMAPPED.equals(category.getInstructorType())
                    ? category.getInstructorType() + " (context only)"
                    : category.getInstructorType(),
                category.getPlannedCost(),
                category.getPlannedCount(),
                category.getActualPeople(),
                category.getActualFte(),
                category.getComparableSalary(),
                category.getDifference(),
                percentDifference(category.getDifference(), category.getPlannedCost())));
        }

        writeBannerTaCheck(sheet);
        writeByCategoryDataDictionary(sheet);
    }

    /**
     * The one aggregate that earns a place in the workbook. TAs are ~58% of compared salary (FY2026,
     * L&S-wide), and this is the only independent count of them — everything else stays on the JSON
     * endpoint. Banner counts TA-of-record assignments within the budget's course subjects while
     * Payroll Individuals counts everyone paid as a TA under the department code, so the two are a
     * sanity check on each other, NOT a reconciliation: they are not expected to be equal.
     */
    private void writeBannerTaCheck(Sheet sheet) {
        if (reportView.getBannerTaAssignments() == null) {
            return;
        }

        writeBlankRow(sheet);
        writeSectionLabel(sheet, "Banner record of TA in budgeted subject codes", BY_CATEGORY_LAST_COLUMN);
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("TA assignments",
            reportView.getBannerTaAssignments()));
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("TA individuals",
            reportView.getBannerTaIndividuals()));
    }

    /** Brief definitions beside the report, so unlike units and data scopes are not read as peers. */
    private void writeByCategoryDataDictionary(Sheet sheet) {
        writeBlankRow(sheet);

        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 12);
        titleStyle.setFont(titleFont);

        String title = String.format("%s, UCPath DOPE FY%d",
            reportView.budgetScenarioHeading(), reportView.getFiscalYear());
        Row titleRow = writeSectionLabel(sheet, title, BY_CATEGORY_LAST_COLUMN);
        applyStyle(titleRow, 0, BY_CATEGORY_LAST_COLUMN, titleStyle);

        CellStyle labelStyle = sheet.getWorkbook().createCellStyle();
        Font labelFont = sheet.getWorkbook().createFont();
        labelFont.setBold(true);
        labelStyle.setFont(labelFont);
        labelStyle.setVerticalAlignment(VerticalAlignment.TOP);

        CellStyle descriptionStyle = sheet.getWorkbook().createCellStyle();
        descriptionStyle.setWrapText(true);
        descriptionStyle.setVerticalAlignment(VerticalAlignment.TOP);

        for (List<String> definition : BY_CATEGORY_DATA_DICTIONARY) {
            Row row = sheet.createRow(sheet.getLastRowNum() + 1);
            Cell label = row.createCell(0);
            label.setCellValue(definition.get(0));
            label.setCellStyle(labelStyle);

            Cell description = row.createCell(1);
            description.setCellValue(definition.get(1));
            description.setCellStyle(descriptionStyle);
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(),
                1, BY_CATEGORY_LAST_COLUMN));
            row.setHeightInPoints(30);
        }
    }

    private static void writeBlankRow(Sheet sheet) {
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList("", ""));
    }

    /* Merged, so a long heading can't stretch column A: autoSizeColumn skips merged cells but measures
       every other row in a column, not just the header. Metric labels below stay short for that reason. */
    private static Row writeSectionLabel(Sheet sheet, String label, int lastColumn) {
        Row row = sheet.createRow(sheet.getLastRowNum() + 1);
        row.createCell(0).setCellValue(label);
        sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, lastColumn));
        return row;
    }

    private static void applyStyle(Row row, int firstColumn, int lastColumn, CellStyle style) {
        for (int column = firstColumn; column <= lastColumn; column++) {
            Cell cell = row.getCell(column);
            if (cell == null) {
                cell = row.createCell(column);
            }
            cell.setCellStyle(style);
        }
    }

    /* planned (IPA) vs actual (Banner) staffing per course, flat with a blank row between courses */
    void buildByCourseSheet(Sheet sheet) {
        // The "Staffing by course: <scenario> (2025-2026)" title row was removed 2026-07-30, so headers
        // are now row 0. The scenario and payroll year remain visible in the By Category dictionary
        // heading without displacing either tab's data headers.
        ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
            "Term", "Subject", "Course", "Title", "Source", "Name", "Role", "DOPE Job Code",
            "Sections", "Term FTE"));

        // One row per person, with Term/Subject/Course repeated on each so it stands alone, and a blank
        // row between courses to make the tab scannable. The course header row and the collapsible
        // outline it anchored were both removed 2026-07-29: once the identity repeats, the header row
        // carried nothing unique, and a group with no header above it collapses to nothing useful.
        boolean firstCourse = true;
        for (CourseStaffingView course : reportView.getCourses()) {
            if (!firstCourse) {
                writeBlankRow(sheet);
            }
            firstCourse = false;

            String term = course.getTermCode();
            String subject = course.getSubjectCode();
            String number = course.getCourseNumber();
            String title = course.getTitle();

            // A budgeted course with nobody assigned has no person rows at all, and without the old
            // header row it would vanish from the tab entirely — so give it an identity-only row.
            if (course.getPlanned().isEmpty() && course.getActual().isEmpty()) {
                ExcelHelper.writeRowToSheet(sheet, Arrays.asList(term, subject, number, title));
            }

            for (CourseStaffingPersonView planned : course.getPlanned()) {
                ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                    term, subject, number, title, "IPA", planned.getName(), planned.getRole()));
            }
            for (CourseStaffingPersonView actual : course.getActual()) {
                ExcelHelper.writeRowToSheet(sheet, Arrays.asList(
                    term, subject, number, title, "Banner", actual.getName(), actual.getRole(),
                    actual.getDopeJobCode(), actual.getSections(), actual.getFte()));
            }
        }

        // autosize per column (default ignores the merged title, so column A stays narrow)
        for (int column = 0; column <= BY_COURSE_LAST_COLUMN; column++) {
            sheet.autoSizeColumn(column);
        }
    }

    private static String percentDifference(BigDecimal difference, BigDecimal plannedCost) {
        if (difference == null || plannedCost == null || plannedCost.compareTo(BigDecimal.ZERO) == 0) {
            return "";
        }

        return difference.multiply(new BigDecimal("100"))
            .divide(plannedCost, 1, RoundingMode.HALF_UP) + "%";
    }
}
