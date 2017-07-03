package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.entities.*;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;

public class BudgetView {
    String workgroupId;
    List<BudgetScenario> budgetScenarios;
    List<SectionGroupCost> sectionGroupCosts;
    List<LineItem> lineItems;
    Budget budget;
    List<LineItemCategory> lineItemCategories;

    public BudgetView(
            List<BudgetScenario> budgetScenarios,
            List<SectionGroupCost> sectionGroupCosts,
            List<LineItem> lineItems,
            Budget budget,
            List<LineItemCategory> lineItemCategories) {
        setBudgetScenarios(budgetScenarios);
        setSectionGroupCosts(sectionGroupCosts);
        setLineItems(lineItems);
        setBudget(budget);
        setLineItemCategories(lineItemCategories);
    }

    public String getWorkgroupId() {
        return workgroupId;
    }

    public void setWorkgroupId(String workgroupId) {
        this.workgroupId = workgroupId;
    }

    public List<BudgetScenario> getBudgetScenarios() {
        return budgetScenarios;
    }

    public void setBudgetScenarios(List<BudgetScenario> budgetScenarios) {
        this.budgetScenarios = budgetScenarios;
    }

    public List<SectionGroupCost> getSectionGroupCosts() {
        return sectionGroupCosts;
    }

    public void setSectionGroupCosts(List<SectionGroupCost> sectionGroupCosts) {
        this.sectionGroupCosts = sectionGroupCosts;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public List<LineItemCategory> getLineItemCategories() {
        return lineItemCategories;
    }

    public void setLineItemCategories(List<LineItemCategory> lineItemCategories) {
        this.lineItemCategories = lineItemCategories;
    }
}