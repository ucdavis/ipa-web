package edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views;

import java.util.List;

public class StaffingByCourseReportView {
    long workgroupId;
    long year;
    String budgetScenarioName;
    List<CourseStaffingView> courses;
    // departmental actual-cost + name-match coverage; null when the Datamart isn't available
    StaffingCostSummaryView costSummary;

    public StaffingByCourseReportView(long workgroupId, long year, String budgetScenarioName,
                                      List<CourseStaffingView> courses, StaffingCostSummaryView costSummary) {
        this.workgroupId = workgroupId;
        this.year = year;
        this.budgetScenarioName = budgetScenarioName;
        this.courses = courses;
        this.costSummary = costSummary;
    }

    public long getWorkgroupId() {
        return workgroupId;
    }

    public long getYear() {
        return year;
    }

    public String getBudgetScenarioName() {
        return budgetScenarioName;
    }

    public List<CourseStaffingView> getCourses() {
        return courses;
    }

    public StaffingCostSummaryView getCostSummary() {
        return costSummary;
    }
}
