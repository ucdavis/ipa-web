package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.util.ArrayList;
import java.util.List;

/**
 * The approved budget reconciled against actuals, in two views of the same year.
 *
 * categories (By Category) is scoped to a payroll department: all payroll in that DEPT_CD,
 * instructional or not. courses (By Course) is scoped to the budget scenario's course subjects, which
 * includes people paid by other departments. The two views cover different populations and are not
 * expected to tie.
 */
public class BudgetReconciliationReportView {
    long workgroupId;
    String workgroupCode;
    String fteDepartmentName;
    long year;
    int fiscalYear;
    String departmentCode;
    String budgetScenarioName;
    PlannedTotalsView plannedTotals;
    List<BudgetReconciliationCategoryView> categories;
    List<CourseStaffingView> courses;
    StaffingCostSummaryView costSummary;
    Integer bannerTaAssignments, bannerTaIndividuals;

    public BudgetReconciliationReportView(long workgroupId, String workgroupCode,
                                          String fteDepartmentName, long year,
                                          int fiscalYear, String departmentCode,
                                          String budgetScenarioName,
                                          PlannedTotalsView plannedTotals,
                                          List<BudgetReconciliationCategoryView> categories,
                                          List<CourseStaffingView> courses,
                                          StaffingCostSummaryView costSummary,
                                          Integer bannerTaAssignments,
                                          Integer bannerTaIndividuals) {
        this.workgroupId = workgroupId;
        this.workgroupCode = workgroupCode;
        this.fteDepartmentName = fteDepartmentName;
        this.year = year;
        this.fiscalYear = fiscalYear;
        this.departmentCode = departmentCode;
        this.budgetScenarioName = budgetScenarioName;
        this.plannedTotals = plannedTotals;
        this.categories = categories;
        this.courses = courses;
        this.costSummary = costSummary;
        this.bannerTaAssignments = bannerTaAssignments;
        this.bannerTaIndividuals = bannerTaIndividuals;
    }

    public long getWorkgroupId() {
        return workgroupId;
    }

    public String getWorkgroupCode() {
        return workgroupCode;
    }

    /** the Budget Office's FTE Department (e.g. "German &amp; Russian"); names the workbook file */
    public String getFteDepartmentName() {
        return fteDepartmentName;
    }

    public long getYear() {
        return year;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getBudgetScenarioName() {
        return budgetScenarioName;
    }

    /** Workbook heading: workgroup + scenario, comma-separated when the payroll plan is combined. */
    String budgetScenarioHeading() {
        String[] scenarios = budgetScenarioName.split("; ", -1);
        if (scenarios.length == 1) {
            return workgroupCode + " " + budgetScenarioName;
        }

        List<String> labels = new ArrayList<>();
        for (String scenario : scenarios) {
            int separator = scenario.indexOf(": ");
            labels.add(separator < 0
                ? scenario
                : scenario.substring(0, separator) + " " + scenario.substring(separator + 2));
        }
        return String.join(", ", labels);
    }

    public PlannedTotalsView getPlannedTotals() {
        return plannedTotals;
    }

    public List<BudgetReconciliationCategoryView> getCategories() {
        return categories;
    }

    public List<CourseStaffingView> getCourses() {
        return courses;
    }

    public StaffingCostSummaryView getCostSummary() {
        return costSummary;
    }

    /**
     * One per TA-section-term, so a TA on three sections counts three times. Reported apart from the
     * TAs category row, which invited reading it as a headcount.
     */
    public Integer getBannerTaAssignments() {
        return bannerTaAssignments;
    }

    /**
     * Counted by course subject while the payroll figures are counted by department code, so the two
     * only roughly agree.
     */
    public Integer getBannerTaIndividuals() {
        return bannerTaIndividuals;
    }
}
