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
    SectionGroup sectionGroup;
    BudgetScenario budgetScenario;
    private long enrollment, taCount, sectionCount, readerCount, instructorCost;
    Instructor instructor;
    Instructor originalInstructor;

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
    @JoinColumn(name = "SectionGroupId", nullable = false)
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

    public long getInstructorCost() {
        return instructorCost;
    }

    public void setInstructorCost(long instructorCost) {
        this.instructorCost = instructorCost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", nullable = false)
    @NotNull
    @JsonIgnore
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OriginalInstructorId", nullable = false)
    @NotNull
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
}