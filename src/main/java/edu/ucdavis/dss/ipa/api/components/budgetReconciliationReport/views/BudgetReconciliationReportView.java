package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.util.List;

/**
 * The approved budget reconciled against actuals, in two views of the same year.
 *
 * categories (By Category) rolls up by instructor type and is scoped to a payroll department —
 * ALL payroll in that DEPT_CD, instructional or not. courses (By Course) pairs planned instructors
 * with Banner's actual assignments and is scoped to the budget scenario's course subjects, which
 * includes people paid by other departments.
 *
 * The two views therefore cover different populations and are NOT expected to tie. costSummary
 * quantifies the gap: how much of the department's payroll was attributed to a course, and how many
 * instructors fell outside the department's payroll entirely.
 */
public class BudgetReconciliationReportView {
    long workgroupId;
    long year;
    int fiscalYear;
    String departmentCode;
    String budgetScenarioName;
    List<BudgetReconciliationCategoryView> categories;
    List<CourseStaffingView> courses;
    StaffingCostSummaryView costSummary;

    public BudgetReconciliationReportView(long workgroupId, long year, int fiscalYear,
                                          String departmentCode, String budgetScenarioName,
                                          List<BudgetReconciliationCategoryView> categories,
                                          List<CourseStaffingView> courses,
                                          StaffingCostSummaryView costSummary) {
        this.workgroupId = workgroupId;
        this.year = year;
        this.fiscalYear = fiscalYear;
        this.departmentCode = departmentCode;
        this.budgetScenarioName = budgetScenarioName;
        this.categories = categories;
        this.courses = courses;
        this.costSummary = costSummary;
    }

    public long getWorkgroupId() {
        return workgroupId;
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

    public List<BudgetReconciliationCategoryView> getCategories() {
        return categories;
    }

    /** By Course rows; planned-only when Banner isn't configured */
    public List<CourseStaffingView> getCourses() {
        return courses;
    }

    /** cost-match diagnostics; null when Banner isn't configured, leaving nothing to match */
    public StaffingCostSummaryView getCostSummary() {
        return costSummary;
    }
}
