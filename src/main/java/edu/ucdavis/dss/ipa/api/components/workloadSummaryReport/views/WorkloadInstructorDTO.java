package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

public class WorkloadInstructorDTO {
    String department, instructorType, name, term, courseType, description, offering;

    public WorkloadInstructorDTO(String department, String instructorType, String name, String term, String courseType,
                                 String description, String offering) {
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
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
}
