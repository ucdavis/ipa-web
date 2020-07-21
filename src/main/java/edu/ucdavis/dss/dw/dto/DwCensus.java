package edu.ucdavis.dss.dw.dto;

import static edu.ucdavis.dss.ipa.api.helpers.Utilities.isNumeric;

public class DwCensus {
    String courseNumber, subjectCode, snapshotCode, sequenceNumber, crn, termCode;
    long currentEnrolledCount, maxEnrollmentCount, waitCount, currentAvailableSeatCount, availableWaitCount, studentCount;

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

    public String getSnapshotCode() {
        return snapshotCode;
    }

    public void setSnapshotCode(String snapshotCode) {
        this.snapshotCode = snapshotCode;
    }

    public String getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    public long getCurrentEnrolledCount() {
        return currentEnrolledCount;
    }

    public void setCurrentEnrolledCount(long currentEnrolledCount) {
        this.currentEnrolledCount = currentEnrolledCount;
    }

    public long getMaxEnrollmentCount() {
        return maxEnrollmentCount;
    }

    public void setMaxEnrollmentCount(long maxEnrollmentCount) {
        this.maxEnrollmentCount = maxEnrollmentCount;
    }

    public long getWaitCount() {
        return waitCount;
    }

    public void setWaitCount(long waitCount) {
        this.waitCount = waitCount;
    }

    public long getCurrentAvailableSeatCount() {
        return currentAvailableSeatCount;
    }

    public void setCurrentAvailableSeatCount(long currentAvailableSeatCount) {
        this.currentAvailableSeatCount = currentAvailableSeatCount;
    }

    public long getAvailableWaitCount() {
        return availableWaitCount;
    }

    public void setAvailableWaitCount(long availableWaitCount) {
        this.availableWaitCount = availableWaitCount;
    }

    public long getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(long studentCount) {
        this.studentCount = studentCount;
    }

    /**
     * @return First letter for lettered sections or string of numbers for numeric sections.
     */
    public String getSequencePattern() {
        if (isNumeric(this.getSequenceNumber())) {
            return this.getSequenceNumber();
        } else {
            return String.valueOf(this.getSequenceNumber().charAt(0));
        }
    }
}
