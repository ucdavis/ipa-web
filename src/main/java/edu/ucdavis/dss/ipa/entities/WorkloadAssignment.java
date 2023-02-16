package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.List;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

@Entity
@Table(name = "WorkloadAssignments")
public class WorkloadAssignment extends BaseEntity {
    private long id;
    private WorkloadSnapshot workloadSnapshot;
    private String department, instructorType, name, termCode, courseType, description, offering, lastOfferedCensus,
        units,
        instructorNote;
    private Long census, previousYearCensus;
    private Integer plannedSeats;
    private Float studentCreditHours;
    private long year;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkloadSnapshotId", nullable = false)
    @NotNull
    @JsonIgnore
    public WorkloadSnapshot getWorkloadSnapshot() {
        return workloadSnapshot;
    }

    public void setWorkloadSnapshot(WorkloadSnapshot workloadSnapshot) {
        this.workloadSnapshot = workloadSnapshot;
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

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
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

    public String getLastOfferedCensus() {
        return lastOfferedCensus;
    }

    public void setLastOfferedCensus(String lastOfferedCensus) {
        this.lastOfferedCensus = lastOfferedCensus;
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

    public long getYear() {
        return year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    @Transient
    public List<Object> toList() {
        return Arrays.asList(
            this.year == 0 ? "" : this.year + "-" + String.valueOf(this.year + 1).substring(2, 4),
            this.department,
            this.instructorType.toUpperCase(),
            this.name,
            Term.getFullName(termCode),
            this.courseType,
            this.description,
            this.offering,
            this.census,
            this.plannedSeats,
            this.previousYearCensus,
            this.lastOfferedCensus,
            this.units,
            this.studentCreditHours,
            this.instructorNote
        );
    }
}
