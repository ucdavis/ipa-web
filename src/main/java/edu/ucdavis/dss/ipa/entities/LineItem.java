package edu.ucdavis.dss.ipa.entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.LineItemDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "LineItems")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = LineItemDeserializer.class)
public class LineItem extends BaseEntity {
    private long id;
    private BudgetScenario budgetScenario;
    private BigDecimal amount = new BigDecimal(0);
    private String description, notes;
    private LineItemCategory lineItemCategory;
    private List<LineItemComment> lineItemComments = new ArrayList<>();
    private Boolean hidden = false;
    private TeachingAssignment teachingAssignment;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
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

    /**
     * May reference a teachingAssignment that no longer exists. Orphaning is intentional to allow user to decide whether or not to delete.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TeachingAssignmentId", nullable = true)
    @JsonIgnore
    public TeachingAssignment getTeachingAssignment() {
        return teachingAssignment;
    }

    public void setTeachingAssignment(TeachingAssignment teachingAssignment) {
        this.teachingAssignment = teachingAssignment;
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

    @NotNull
    @JsonProperty
    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
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

    @JsonProperty("teachingAssignmentId")
    @Transient
    public Long getTeachingAssignmentIdIfExists() {
        if(teachingAssignment != null) {
            return teachingAssignment.getId();
        } else {
            return null;
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

    @JsonProperty("lastModifiedBy")
    @Transient
    public String getLastModifiedBy() {
        return modifiedBy;
    }

    @JsonProperty("lastModifiedOn")
    @Transient
    public Date getLastModifiedOn() {
        return updatedAt;
    }

    @JsonProperty("createdAt")
    @Transient
    public Date getCreatedOn() {
        return createdAt;
    }

    /**
     * Returns a loginId or null.
     * ModifiedBy is expected to come in the form "system" or "user:guilden", or null for older entities that haven't been updated recently.
     *
     * @return
     */
    @JsonIgnore
    @Transient
    public String getLastModifiedByAsLoginId() {
        if (modifiedBy == null) {
            return null;
        }

        int modifiedByIndex = modifiedBy.indexOf(":");

        if (modifiedByIndex == -1) {
            return null;
        }

        return modifiedBy.substring(modifiedByIndex + 1);
    }

}
