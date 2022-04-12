package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import java.util.Arrays;
import java.util.List;

public class WorkloadInstructorDTO {
    String department, instructorType, name, term, courseType, description, offering, lastOfferedCensus, units,
        instructorNote;
    Long census, previousYearCensus;
    Integer plannedSeats;
    Float studentCreditHours;
    long year;

    public WorkloadInstructorDTO(long year, String department, String instructorType, String name, String term,
                                 String courseType,
                                 String description, String offering, Long census, Integer plannedSeats,
                                 Long previousYearCensus,
                                 String lastOfferedCensus, String units,
                                 Float studentCreditHours, String instructorNote) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
        this.census = census;
        this.plannedSeats = plannedSeats;
        this.previousYearCensus = previousYearCensus;
        this.lastOfferedCensus = lastOfferedCensus;
        this.units = units;
        this.studentCreditHours = studentCreditHours;
        this.instructorNote = instructorNote;
    }

    public WorkloadInstructorDTO(long year, String department, String instructorType, String name) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
    }

    public WorkloadInstructorDTO(long year, String department, String instructorType, String name, String term,
                                 String courseType,
                                 String description, String offering) {
        this.year = year;
        this.department = department;
        this.instructorType = instructorType;
        this.name = name;
        this.term = term;
        this.courseType = courseType;
        this.description = description;
        this.offering = offering;
    }

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
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

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getInstructorNote() {
        return instructorNote;
    }

    public void setInstructorNote(String instructorNote) {
        this.instructorNote = instructorNote;
    }

    public Long getCensus() {
        return census;
    }

    public void setCensus(Long census) {
        this.census = census;
    }

    public Long getPreviousYearCensus() {
        return previousYearCensus;
    }

    public void setPreviousYearCensus(Long previousYearCensus) {
        this.previousYearCensus = previousYearCensus;
    }

    public String getLastOfferedCensus() {
        return lastOfferedCensus;
    }

    public void setLastOfferedCensus(String lastOfferedCensus) {
        this.lastOfferedCensus = lastOfferedCensus;
    }

    public Integer getPlannedSeats() {
        return plannedSeats;
    }

    public void setPlannedSeats(Integer plannedSeats) {
        this.plannedSeats = plannedSeats;
    }

    public Float getStudentCreditHours() {
        return studentCreditHours;
    }

    public void setStudentCreditHours(Float studentCreditHours) {
        this.studentCreditHours = studentCreditHours;
    }

    public List<Object> toList() {
        return Arrays.asList(
            this.year + "-" + String.valueOf(this.year + 1).substring(2, 4),
            this.getDepartment(),
            this.getInstructorType().toUpperCase(),
            this.getName(),
            this.getTerm(),
            this.getCourseType(),
            this.getDescription(),
            this.getOffering(),
            this.getCensus(),
            this.getPlannedSeats(),
            this.getPreviousYearCensus(),
            this.getLastOfferedCensus(),
            this.getUnits(),
            this.getStudentCreditHours(),
            this.getInstructorNote()
        );
    }
}
