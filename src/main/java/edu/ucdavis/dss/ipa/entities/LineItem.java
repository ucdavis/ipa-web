package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.ActivityDeserializer;
import edu.ucdavis.dss.ipa.api.deserializers.LineItemDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "LineItems")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = LineItemDeserializer.class)
public class LineItem extends BaseEntity {
    private long id;
    private BudgetScenario budgetScenario;
    private float amount = 0f;
    private String description, notes;
    private LineItemCategory lineItemCategory;
    private List<LineItemComment> lineItemComments = new ArrayList<>();

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
    @JoinColumn(name = "BudgetScenarioId", nullable = false)
    @NotNull
    @JsonIgnore
    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LineItemCategoryId", nullable = false)
    @NotNull
    @JsonIgnore
    public LineItemCategory getLineItemCategory() {
        return lineItemCategory;
    }

    public void setLineItemCategory(LineItemCategory lineItemCategory) {
        this.lineItemCategory = lineItemCategory;
    }

    @JsonIgnore
    @OneToMany(mappedBy="lineItem", cascade=CascadeType.ALL, orphanRemoval = true)
    public List<LineItemComment> getLineItemComments() {
        return lineItemComments;
    }

    public void setLineItemComments(List<LineItemComment> lineItemComments) {
        this.lineItemComments = lineItemComments;
    }

    @JsonProperty("lineItemCategoryId")
    @Transient
    public long getLineItemCategoryId() {
        if(lineItemCategory != null) {
            return lineItemCategory.getId();
        } else {
            return 0;
        }
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