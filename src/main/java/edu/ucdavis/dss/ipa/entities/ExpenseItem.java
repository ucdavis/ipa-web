package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.ExpenseItemDeserializer;
import edu.ucdavis.dss.ipa.api.deserializers.LineItemDeserializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "ExpenseItems")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = ExpenseItemDeserializer.class)
public class ExpenseItem {
    private long id;
    private BudgetScenario budgetScenario;
    private BigDecimal amount = new BigDecimal(0);
    private String description;
    private ExpenseItemCategory expenseItemCategory;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpenseItemCategoryId", nullable = false)
    @NotNull
    @JsonIgnore
    public ExpenseItemCategory getExpenseItemCategory() {
        return expenseItemCategory;
    }

    public void setExpenseItemCategory(ExpenseItemCategory expenseItemCategory) {
        this.expenseItemCategory = expenseItemCategory;
    }

    @JsonProperty("expenseItemCategoryId")
    @Transient
    public long getExpenseItemCategoryId() {
        if(expenseItemCategory != null) {
            return expenseItemCategory.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("categoryDescription")
    @Transient
    public String getExpenseItemCategoryDescription() {
        if(expenseItemCategory != null) {
            return expenseItemCategory.getDescription();
        } else {
            return "";
        }
    }
}
