package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.entities.*;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.List;

public class BudgetScenarioView {
    BudgetScenario budgetScenario;
    List<SectionGroupCost> sectionGroupCosts;
    List<LineItem> lineItems;
    List<SectionGroupCostComment> sectionGroupCostComments;
    List<SectionGroupCostInstructor> sectionGroupCostInstructors;
    List<LineItemComment> lineItemComments;
    List<InstructorCost> instructorCosts;
    List<InstructorTypeCost> instructorTypeCosts;

    public BudgetScenarioView(
        BudgetScenario budgetScenario,
        List<SectionGroupCost> sectionGroupCosts,
        List<SectionGroupCostComment> sectionGroupCostComments,
        List<SectionGroupCostInstructor> sectionGroupCostInstructors,
        List<LineItem> lineItems,
        List<LineItemComment> lineItemComments,
        List<InstructorCost> instructorCosts,
        List<InstructorTypeCost> instructorTypeCosts) {
        setBudgetScenario(budgetScenario);
        setSectionGroupCosts(sectionGroupCosts);
        setLineItems(lineItems);
        setSectionGroupCostComments(sectionGroupCostComments);
        setSectionGroupCostInstructors(sectionGroupCostInstructors);
        setLineItemComments(lineItemComments);
        setInstructorCosts(instructorCosts);
        setInstructorTypeCosts(instructorTypeCosts);
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

    public List<SectionGroupCostInstructor> getSectionGroupCostInstructors() {
        return sectionGroupCostInstructors;
    }

    public void setSectionGroupCostInstructors (List<SectionGroupCostInstructor> sectionGroupCostInstructors) {
        this.sectionGroupCostInstructors = sectionGroupCostInstructors;
    }

    public List<LineItemComment> getLineItemComments() {
        return lineItemComments;
    }

    public void setLineItemComments(List<LineItemComment> lineItemComments) {
        this.lineItemComments = lineItemComments;
    }

    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(
        List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    public List<InstructorTypeCost> getInstructorTypeCosts() {
        return instructorTypeCosts;
    }

    public void setInstructorTypeCosts(
        List<InstructorTypeCost> instructorTypeCosts) {
        this.instructorTypeCosts = instructorTypeCosts;
    }
}
