package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

public class WorkloadInstructorDTO {
    String department, instructorType, name, term, courseType, description, offering, census, instructorNote;
    Float units, studentCreditHours;

    public WorkloadInstructorDTO(String department, String instructorType, String name, String term, String courseType,
                                 String description, String offering, String census, Float units,
                                 Float studentCreditHours, String instructorNote) {
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
        this.census = census;
        this.units = units;
        this.studentCreditHours = studentCreditHours;
        this.instructorNote = instructorNote;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getInstructorType() {
        return instructorType;
    }

    public void setInstructorType(String instructorType) {
        this.instructorType = instructorType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getCourseType() {
        return courseType;
    }

    public void setCourseType(String courseType) {
        this.courseType = courseType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOffering() {
        return offering;
    }

    public void setOffering(String offering) {
        this.offering = offering;
    }

    public String getCensus() {
        return census;
    }

    public void setCensus(String census) {
        this.census = census;
    }

    public String getInstructorNote() {
        return instructorNote;
    }

    public void setInstructorNote(String instructorNote) {
        this.instructorNote = instructorNote;
    }

    public Float getUnits() {
        return units;
    }

    public void setUnits(Float units) {
        this.units = units;
    }

    public Float getStudentCreditHours() {
        return studentCreditHours;
    }

    public void setStudentCreditHours(Float studentCreditHours) {
        this.studentCreditHours = studentCreditHours;
    }
}
