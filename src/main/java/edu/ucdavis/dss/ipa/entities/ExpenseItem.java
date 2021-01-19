package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.ExpenseItemDeserializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "ExpenseItems")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = ExpenseItemDeserializer.class)
public class ExpenseItem extends BaseEntity {
    private long id;
    private BudgetScenario budgetScenario;
    private BigDecimal amount = BigDecimal.ZERO;
    private String description;
    private ExpenseItemType expenseItemType;
    private String termCode;

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
    @JsonProperty("budgetScenarioId")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
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

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ExpenseItemTypeId", nullable = false)
    @NotNull
    @JsonIgnore
    public ExpenseItemType getExpenseItemType() {
        return expenseItemType;
    }

    public void setExpenseItemType(ExpenseItemType expenseItemType) {
        this.expenseItemType = expenseItemType;
    }

    @JsonProperty("expenseItemTypeId")
    @Transient
    public long getExpenseItemTypeId() {
        if(expenseItemType != null) {
            return expenseItemType.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("typeDescription")
    @Transient
    public String getExpenseItemTypeDescription() {
        if(expenseItemType != null) {
            return expenseItemType.getDescription();
        } else {
            return "";
        }
    }

    @JsonProperty("termDescription")
    @Transient
    public String getTermDescription() {
        if(termCode != null) {
            return Term.getRegistrarName(termCode);
        } else {
            return "";
        }
    }
}
