package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.util.List;

/**
 * Planned vs actual staffing for one course (subject + course number in a term). planned comes from
 * the IPA budget scenario; actual from Banner instructional assignments. Either list may be empty
 * (planned-but-not-staffed, or staffed-but-not-planned).
 */
public class CourseStaffingView {
    String termCode;
    String subjectCode;
    String courseNumber;
    String title;
    List<CourseStaffingPersonView> planned;
    List<CourseStaffingPersonView> actual;

    public CourseStaffingView(String termCode, String subjectCode, String courseNumber, String title,
                              List<CourseStaffingPersonView> planned,
                              List<CourseStaffingPersonView> actual) {
        this.termCode = termCode;
        this.subjectCode = subjectCode;
        this.courseNumber = courseNumber;
        this.title = title;
        this.planned = planned;
        this.actual = actual;
    }

    public String getTermCode() {
        return termCode;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    /**
     * The course title from the budget scenario's own `SectionGroupCost.Title`. **Null for a course that
     * exists only on the Banner side** — an unbudgeted course has no IPA row to read a title from, and
     * ZIVASGN carries no title, so filling it would mean joining further Banner tables into a query the
     * DB load constrains.
     */
    public String getTitle() {
        return title;
    }

    public List<CourseStaffingPersonView> getPlanned() {
        return planned;
    }

    public List<CourseStaffingPersonView> getActual() {
        return actual;
    }
}
