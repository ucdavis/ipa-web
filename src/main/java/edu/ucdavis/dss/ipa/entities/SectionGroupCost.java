package edu.ucdavis.dss.ipa.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Budget is used to record information common to all budget scenarios.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "SectionGroupCosts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SectionGroupCost {
    private long id;
    private SectionGroup sectionGroup;
    private BudgetScenario budgetScenario;
    private long enrollment = 0, taCount = 0, sectionCount = 0, readerCount = 0;
    private Instructor instructor;
    private Instructor originalInstructor;
    private String title, subjectCode, courseNumber, effectiveTermCode, termCode, sequencePattern;
    private float unitsHigh, unitsLow, instructorCost = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionGroupId", nullable = true)
    @NotNull
    @JsonIgnore
    public SectionGroup getSectionGroup() {
        return sectionGroup;
    }

    public void setSectionGroup(SectionGroup sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    public long getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(long enrollment) {
        this.enrollment = enrollment;
    }

    public long getTaCount() {
        return taCount;
    }

    public void setTaCount(long taCount) {
        this.taCount = taCount;
    }

    public long getSectionCount() {
        return sectionCount;
    }

    public void setSectionCount(long sectionCount) {
        this.sectionCount = sectionCount;
    }

    public long getReaderCount() {
        return readerCount;
    }

    public void setReaderCount(long readerCount) {
        this.readerCount = readerCount;
    }

    public float getInstructorCost() {
        return instructorCost;
    }

    public void setInstructorCost(float instructorCost) {
        this.instructorCost = instructorCost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", nullable = true)
    @JsonIgnore
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OriginalInstructorId", nullable = true)
    @JsonIgnore
    public Instructor getOriginalInstructor() {
        return originalInstructor;
    }

    public void setOriginalInstructor(Instructor originalInstructor) {
        this.originalInstructor = originalInstructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BudgetScenarioId", nullable = false)
    @NotNull
    @JsonIgnore
    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

    public float getUnitsHigh() {
        return unitsHigh;
    }

    public void setUnitsHigh(float unitsHigh) {
        this.unitsHigh = unitsHigh;
    }

    public float getUnitsLow() {
        return unitsLow;
    }

    public void setUnitsLow(float unitsLow) {
        this.unitsLow = unitsLow;
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

    public String getEffectiveTermCode() {
        return effectiveTermCode;
    }

    public void setEffectiveTermCode(String effectiveTermCode) {
        this.effectiveTermCode = effectiveTermCode;
    }

    public String getSequencePattern() {
        return sequencePattern;
    }

    public void setSequencePattern(String sequencePattern) {
        this.sequencePattern = sequencePattern;
    }

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    @JsonProperty("budgetScenarioId")
    @Transient
    public long getBudgetScenarioId() {
        if(budgetScenario != null) {
            return budgetScenario.getId();
        } else {
            return 0;
        }
    }
}