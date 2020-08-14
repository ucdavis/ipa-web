package edu.ucdavis.dss.ipa.entities;

import java.util.ArrayList;
import java.util.List;

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
@Table(name = "Budgets")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Budget extends BaseEntity {
    private long id;
    private Schedule schedule;
    private float taCost = 0f, readerCost = 0f;
    private List<BudgetScenario> budgetScenarios = new ArrayList<>();
    private List<InstructorCost> instructorCosts = new ArrayList<>();
    private List<InstructorTypeCost> instructorTypeCosts = new ArrayList<>();

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
    @JoinColumn(name = "ScheduleId", nullable = false)
    @NotNull
    @JsonIgnore
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public float getTaCost() {
        return taCost;
    }

    public void setTaCost(float taCost) {
        this.taCost = taCost;
    }

    public float getReaderCost() {
        return readerCost;
    }

    public void setReaderCost(float readerCost) {
        this.readerCost = readerCost;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "budget", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<BudgetScenario> getBudgetScenarios() {
        return budgetScenarios;
    }

    public void setBudgetScenarios(List<BudgetScenario> budgetScenarios) {
        this.budgetScenarios = budgetScenarios;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "budget")
    @JsonIgnore
    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "budget")
    @JsonIgnore
    public List<InstructorTypeCost> getInstructorTypeCosts() {
        return instructorTypeCosts;
    }

    public void setInstructorTypeCosts(List<InstructorTypeCost> instructorTypeCosts) {
        this.instructorTypeCosts = instructorTypeCosts;
    }
}
