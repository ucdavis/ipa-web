package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.ExpenseItemDeserializer;

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
            String shortTermCode = termCode.substring(4,6);
            switch (shortTermCode){
                case "05":
                    return "Summer Session 1";
                case "06":
                    return "Summer Special Session";
                case "07":
                    return "Summer Session 2";
                case "08":
                    return "Summer Quarter";
                case "09":
                    return "Fall Semester";
                case "10":
                    return "Fall Quarter";
                case "01":
                    return "Winter Quarter";
                case "02":
                    return "Spring Semester";
                case "03":
                    return "Spring Quarter";
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @JsonProperty("budgetScenarioId")
    @Transient
    public long getBudgetScenarioId(){
        if(budgetScenario != null){
            return budgetScenario.getId();
        } else {
            return 0;
        }
    }
}
