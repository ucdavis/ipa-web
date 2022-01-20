package edu.ucdavis.dss.dw.dto;

public class DwSearchResultSection {
    private String title, courseNumber, termCode, effectiveTermCode, sequencePattern;
    private Long seats, creditHoursLow, creditHoursHigh;

    public String getTitle() {
        return title;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public String getTermCode() {
        return termCode;
    }

    public String getEffectiveTermCode() {
        return effectiveTermCode;
    }

    public String getSequencePattern() {
        return sequencePattern;
    }

    public Long getSeats() {
        return seats;
    }

    public Long getCreditHoursLow() {
        return creditHoursLow;
    }

    public Long getCreditHoursHigh() {
        return creditHoursHigh;
    }
}