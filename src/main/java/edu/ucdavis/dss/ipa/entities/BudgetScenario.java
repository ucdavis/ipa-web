package edu.ucdavis.dss.ipa.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.BudgetScenarioDeserializer;

/**
 * Budget is used to record information common to all budget scenarios.
 */

@SuppressWarnings("serial")
@Entity
@JsonDeserialize(using = BudgetScenarioDeserializer.class)
@Table(name = "BudgetScenarios")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BudgetScenario extends BaseEntity {
    private long id;
    private Budget budget;
    private String name, activeTermsBlob;
    private List<SectionGroupCost> sectionGroupCosts = new ArrayList<>();
    private List<LineItem> lineItems = new ArrayList<>();
    private List<ExpenseItem> expenseItems = new ArrayList<>();
    private List<InstructorCost> instructorCosts = new ArrayList<>();
    private List<InstructorTypeCost> instructorTypeCosts = new ArrayList<>();
    private Boolean fromLiveData;
    private Boolean isBudgetRequest = false;
    private Float taCost, readerCost;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "budgetScenario", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<SectionGroupCost> getSectionGroupCosts() {
        return sectionGroupCosts;
    }

    public void setSectionGroupCosts(List<SectionGroupCost> sectionGroupCosts) {
        this.sectionGroupCosts = sectionGroupCosts;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "budgetScenario", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "budgetScenario", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<ExpenseItem> getExpenseItems() {
        return expenseItems;
    }

    public void setExpenseItems(List<ExpenseItem> expenseItems) {
        this.expenseItems = expenseItems;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "budgetScenario", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "budgetScenario", cascade = {CascadeType.ALL})
    @JsonIgnore
    public List<InstructorTypeCost> getInstructorTypeCosts() {
        return instructorTypeCosts;
    }

    public void setInstructorTypeCosts(List<InstructorTypeCost> instructorTypeCosts) {
        this.instructorTypeCosts = instructorTypeCosts;
    }

    /**
     * Terms are expected to be sorted ['01','02','03','04','05','06','07','08','09','10']
     */
    @JsonProperty
    public String getActiveTermsBlob() {
        return activeTermsBlob;
    }

    public void setActiveTermsBlob(String activeTermsBlob) {
        this.activeTermsBlob = activeTermsBlob;
    }

    /**
     * Will set the value for the specified term, '1' for active, '0' for inactive
     * @param termCode
     * @param active
     * @return
     */
    public String setTermInActiveTermsBlob(String termCode, boolean active) {
        if (termCode == null || termCode.length() != 6) {
            return this.activeTermsBlob;
        }

        String term = termCode.substring(termCode.length() -2);

        if (term == null || term.length() != 2) {
            return this.activeTermsBlob;
        }

        int index = Integer.valueOf(term) - 1;
        char value = (active) ? '1' : '0';

        char[] charArrayDTO = this.activeTermsBlob.toCharArray();
        charArrayDTO[index] = value;
        this.activeTermsBlob = String.valueOf(charArrayDTO);

        return this.activeTermsBlob;
    }

    /**
     * Will generate a list of terms from the activeTermsBlob
     * Example: '1010000001' => ['01', '03', '10']
     */
    @Transient
    @JsonProperty("terms")
    public List<String> getActiveTermsBlobAsTerms() {
        List<String> terms = new ArrayList<>();
        List<String> secondYearTerms = new ArrayList<>();

        String termBlob = this.getActiveTermsBlob();

        if (termBlob == null || termBlob.length() != 10) {
            return terms;
        }

        for (int i = 0; i < termBlob.length(); i++) {
            // Skip term '04', its not used
            if (i == 3) { continue; }

            if (termBlob.charAt(i) == '1') {
                String term = String.valueOf(i + 1);

                // Zero pad if necessary
                if (term.length() == 1) {
                    term = "0" + term;
                }

                // Put winter/spring terms at the end to ensure list is chronologically ordered
                if (i < 3) {
                    secondYearTerms.add(term);
                } else {
                    terms.add(term);
                }
            }
        }

        terms.addAll(secondYearTerms);

        return terms;
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

    @JsonProperty
    public Boolean getFromLiveData() {
        return fromLiveData;
    }

    public void setFromLiveData(Boolean fromLiveData) {
        this.fromLiveData = fromLiveData;
    }

    @Transient
    @JsonIgnore
    public String recalculateActiveTermsBlob() {
        for (SectionGroupCost sectionGroupCost : this.sectionGroupCosts) {
            this.setTermInActiveTermsBlob(sectionGroupCost.getTermCode(), true);
        }

        return this.activeTermsBlob;
    }

    public Boolean getIsBudgetRequest() {
        return isBudgetRequest;
    }

    public void setIsBudgetRequest(Boolean budgetRequest) {
        isBudgetRequest = budgetRequest;
    }

    public Float getTaCost() {
        return taCost;
    }

    public void setTaCost(Float taCost) {
        this.taCost = taCost;
    }

    public Float getReaderCost() {
        return readerCost;
    }

    public void setReaderCost(Float readerCost) {
        this.readerCost = readerCost;
    }

    @JsonProperty("creationDate")
    @Transient
    public Date getCreationDate() {
        return createdAt;
    }

    @JsonProperty("lastModifiedOn")
    @Transient
    public Date getLastModifiedOn() {
        return updatedAt;
    }
}
