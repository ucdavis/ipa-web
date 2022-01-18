package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Obada on 10/23/16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DwSection {
    private String crn, title, subjectCode, courseNumber, sequenceNumber, sequencePattern, termCode, effectiveTermCode;
    private long maximumEnrollment;
    private float creditHoursLow, creditHoursHigh;
    private List<DwInstructor> instructors = new ArrayList<>();
    private List<DwActivity> activities = new ArrayList<>();

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

    public String getSequencePattern() {
        return sequencePattern;
    }

    public void setSequencePattern(String sequencePattern) {
        this.sequencePattern = sequencePattern;
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
    public void setMaximumEnrollment(long maximumEnrollment) { this.maximumEnrollment = maximumEnrollment; }

    public List<DwInstructor> getInstructors() {
        return instructors;
    }
    public void setInstructors(List<DwInstructor> instructors) {
        this.instructors = instructors;
    }

    public List<DwActivity> getActivities() {
        return activities;
    }
    public void setActivities(List<DwActivity> activities) {
        this.activities = activities;
    }

    public String getTermCode() {
        return termCode;
    }
    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    public String getEffectiveTermCode() {
        return effectiveTermCode;
    }

    public void setEffectiveTermCode(String effectiveTermCode) {
        this.effectiveTermCode = effectiveTermCode;
    }

    public float getCreditHoursLow() {
        return creditHoursLow;
    }

    public void setCreditHoursLow(float creditHoursLow) {
        this.creditHoursLow = creditHoursLow;
    }

    public float getCreditHoursHigh() {
        return creditHoursHigh;
    }

    public void setCreditHoursHigh(float creditHoursHigh) {
        this.creditHoursHigh = creditHoursHigh;
    }

    /**
     * Calculates a key that will be unique per-section group but not per section
     * e.g. sections A01 and A02 will return the same sorting key
     *
     * @return a section group "sorting key", e.g. "201810-ECS-010-A"
     */
    public String getSectionGroupSortingKey() {
        String sequence = this.getSequenceNumber();

        // If sequence is letter-based, e.g. A01, A02, A03, set sequence simply equal
        // to the letter. A01 will become A, A02 will become A, B01 will become B, etc.
        Character sequenceFirstChar = this.getSequenceNumber().charAt(0);
        if (Character.isLetter(sequenceFirstChar)) {
            sequence = String.valueOf(sequenceFirstChar);
        }

        return this.getTermCode() + "-" + this.getSubjectCode() + "-" + this.getCourseNumber() + "-" + sequence;
    }
}
