package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemComment;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostComment;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;

public class BudgetScenarioView {
    BudgetScenario budgetScenario;
    List<SectionGroupCost> sectionGroupCosts;
    List<LineItem> lineItems;
    List<SectionGroupCostComment> sectionGroupCostComments;
    List<LineItemComment> lineItemComments;

    public BudgetScenarioView(
            BudgetScenario budgetScenario,
            List<SectionGroupCost> sectionGroupCosts,
            List<SectionGroupCostComment> sectionGroupCostComments,
            List<LineItem> lineItems,
            List<LineItemComment> lineItemComments) {
        setBudgetScenario(budgetScenario);
        setSectionGroupCosts(sectionGroupCosts);
        setLineItems(lineItems);
        setSectionGroupCostComments(sectionGroupCostComments);
        setLineItemComments(lineItemComments);
    }

    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
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

    public List<SectionGroupCostComment> getSectionGroupCostComments() {
        return sectionGroupCostComments;
    }

    public void setSectionGroupCostComments(List<SectionGroupCostComment> sectionGroupCostComments) {
        this.sectionGroupCostComments = sectionGroupCostComments;
    }

    public List<LineItemComment> getLineItemComments() {
        return lineItemComments;
    }

    public void setLineItemComments(List<LineItemComment> lineItemComments) {
        this.lineItemComments = lineItemComments;
    }
}