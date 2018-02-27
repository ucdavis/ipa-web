package edu.ucdavis.dss.ipa.entities;

import java.math.BigDecimal;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.InstructorCostDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorCosts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = InstructorCostDeserializer.class)
public class InstructorCost extends BaseEntity {
    private long id;
    private Budget budget;
    private Instructor instructor;
    private BigDecimal cost;
    private Boolean lecturer = false;
    private InstructorTypeCost instructorTypeCost;

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
    @JoinColumn(name = "BudgetId", nullable = false)
    @NotNull
    @JsonIgnore
    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
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
    @JoinColumn(name = "InstructorTypeId", nullable = false)
    @JsonIgnore
    public InstructorTypeCost getInstructorTypeCost() {
        return instructorTypeCost;
    }

    public void setInstructorTypeCost(InstructorTypeCost instructorTypeCost) {
        this.instructorTypeCost = instructorTypeCost;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public Boolean getLecturer() {
        return lecturer;
    }

    public void setLecturer(Boolean lecturer) {
        this.lecturer = lecturer;
    }

    @JsonProperty("instructorId")
    @Transient
    public long getInstructorIdentification() {
        if(instructor != null) {
            return instructor.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("instructorTypeId")
    @Transient
    public long getInstructorTypeIdentification() {
        if(instructorTypeCost != null) {
            return instructorTypeCost.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("budgetId")
    @Transient
    public long getBudgetIdentification() {
        if(budget != null) {
            return budget.getId();
        } else {
            return 0;
        }
    }
}