package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Obada on 10/23/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DwSection {
    private String crn, title, subjectCode, courseNumber, sequenceNumber;
    private long maximumEnrollment;

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getMaximumEnrollment() {
        return maximumEnrollment;
    }

    public void setMaximumEnrollment(long maximumEnrollment) {
        this.maximumEnrollment = maximumEnrollment;
    }
}

