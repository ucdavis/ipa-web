package edu.ucdavis.dss.ipa.entities;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.InstructorTypeCostDeserializer;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorTypeCosts")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = InstructorTypeCostDeserializer.class)
public class InstructorTypeCost extends BaseEntity {
    private long id;
    private Budget budget;
    private Float cost;
    private InstructorType instructorType;
    private List<InstructorCost> instructorCosts = new ArrayList<>();
    private BudgetScenario budgetScenario;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BudgetId", nullable = true)
    @JsonIgnore
    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorTypeId", nullable = false)
    @JsonIgnore
    public InstructorType getInstructorType() {
        return instructorType;
    }

    public void setInstructorType(InstructorType instructorType) {
        this.instructorType = instructorType;
    }

    public Float getCost() {
        return cost;
    }

    public void setCost(Float cost) {
        this.cost = cost;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "instructorTypeCost", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    @Transient
    @JsonProperty("description")
    public String getDescription() {
        if (this.getInstructorType() != null) {
            return this.getInstructorType().getDescription();
        } else {
            return null;
        }
    }

    @JsonProperty("budgetId")
    @Transient
    public Long getBudgetIdentification() {
        if(budget != null) {
            return budget.getId();
        } else {
            return null;
        }
    }

    @JsonProperty("instructorTypeId")
    @Transient
    public Long getInstructorTypeIdIfExists() {
        if(instructorType != null) {
            return instructorType.getId();
        } else {
            return null;
        }
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BudgetScenarioId")
    @JsonIgnore
    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

   @JsonProperty("budgetScenarioId")
   @Transient
    public long getBudgetScenarioIdentification() {
        if(budgetScenario != null) {
            return budgetScenario.getId();
        } else {
            return 0;
        }
    }
}
