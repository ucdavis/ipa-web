package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.util.ArrayList;
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
    String workgroupCode;
    long year;
    int fiscalYear;
    String departmentCode;
    String budgetScenarioName;
    PlannedTotalsView plannedTotals;
    List<BudgetReconciliationCategoryView> categories;
    List<CourseStaffingView> courses;
    StaffingCostSummaryView costSummary;
    Integer bannerTaAssignments, bannerTaIndividuals;

    public BudgetReconciliationReportView(long workgroupId, String workgroupCode, long year,
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

    /** the workgroup's short subject-style code (e.g. "CLA"), used to name the download */
    public String getWorkgroupCode() {
        return workgroupCode;
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

    /** the scenario's own bottom line, so the plan side ties to the approved budget page */
    public PlannedTotalsView getPlannedTotals() {
        return plannedTotals;
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

    /**
     * Banner TA assignment rows for the budgeted subjects and academic-year terms — one per
     * TA-section-term, so a TA on three sections counts three times. Reported alongside the payroll
     * figures rather than on the TAs category row, which invited reading it as a headcount.
     * Null when Banner isn't configured.
     */
    public Integer getBannerTaAssignments() {
        return bannerTaAssignments;
    }

    /**
     * Distinct Banner TA individuals (by PIDM) for the budgeted subjects. Counted by course subject
     * while the payroll figures are counted by department code, so the two only roughly agree.
     */
    public Integer getBannerTaIndividuals() {
        return bannerTaIndividuals;
    }
}
