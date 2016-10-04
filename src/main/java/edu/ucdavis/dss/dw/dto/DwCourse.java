package edu.ucdavis.dss.dw.dto;

/**
 * Created by Lloyd on 10/4/16.
 */
public class DwCourse {
    String subjectCode;
    String effectiveTermCode;
    String title;
    String courseNumber;

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getEffectiveTermCode() {
        return effectiveTermCode;
    }

    public void setEffectiveTermCode(String effectiveTermCode) {
        this.effectiveTermCode = effectiveTermCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

