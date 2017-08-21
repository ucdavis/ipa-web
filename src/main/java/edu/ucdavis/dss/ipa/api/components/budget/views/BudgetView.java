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
    List<SectionGroup> sectionGroups;
    List<Section> sections;
    List<InstructorCost> instructorCosts;
    List<Instructor> instructors;
    List<SectionGroupCostComment> sectionGroupCostComments;
    List<LineItemComment> lineItemComments;
    List<Course> courses;
    List<TeachingAssignment> teachingAssignments;
    List<SupportAssignment> supportAssignments;

    public BudgetView(
            List<BudgetScenario> budgetScenarios,
            List<SectionGroupCost> sectionGroupCosts,
            List<SectionGroupCostComment> sectionGroupCostComments,
            List<LineItem> lineItems,
            List<LineItemComment> lineItemComments,
            Budget budget,
            List<LineItemCategory> lineItemCategories,
            List<SectionGroup> sectionGroups,
            List<Section> sections,
            List<InstructorCost> instructorCosts,
            List<Instructor> instructors,
            List<Course> courses,
            List<TeachingAssignment> teachingAssignments,
            List<SupportAssignment> supportAssignments) {
        setSectionGroups(sectionGroups);
        setSections(sections);
        setBudgetScenarios(budgetScenarios);
        setSectionGroupCosts(sectionGroupCosts);
        setLineItems(lineItems);
        setBudget(budget);
        setLineItemCategories(lineItemCategories);
        setInstructorCosts(instructorCosts);
        setInstructors(instructors);
        setSectionGroupCostComments(sectionGroupCostComments);
        setLineItemComments(lineItemComments);
        setCourses(courses);
        setTeachingAssignments(teachingAssignments);
        setSupportAssignments(supportAssignments);
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

    public List<SectionGroup> getSectionGroups() {
        return sectionGroups;
    }

    public void setSectionGroups(List<SectionGroup> sectionGroups) {
        this.sectionGroups = sectionGroups;
    }

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    public List<Instructor> getInstructors() {
        return instructors;
    }

    public void setInstructors(List<Instructor> instructors) {
        this.instructors = instructors;
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

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<SupportAssignment> getSupportAssignments() {
        return supportAssignments;
    }

    public void setSupportAssignments(List<SupportAssignment> supportAssignments) {
        this.supportAssignments = supportAssignments;
    }
}