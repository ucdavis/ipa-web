package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BudgetView {
    Workgroup workgroup;
    List<BudgetScenario> budgetScenarios;
    List<SectionGroupCost> sectionGroupCosts;
    List<ReasonCategory> reasonCategories;
    List<LineItem> lineItems;
    Budget budget;
    List<LineItemCategory> lineItemCategories;
    List<SectionGroup> sectionGroups;
    List<Section> sections;
    List<InstructorCost> instructorCosts;
    List<Instructor> activeInstructors;
    Set<Instructor> assignedInstructors;
    List<SectionGroupCostComment> sectionGroupCostComments;
    List<SectionGroupCostInstructor> sectionGroupCostInstructors;
    List<LineItemComment> lineItemComments;
    List<Course> courses;
    List<TeachingAssignment> teachingAssignments;
    List<SupportAssignment> supportAssignments;
    Set<User> users;
    List<InstructorTypeCost> instructorTypeCosts;
    List<InstructorType> instructorTypes;
    List<UserRole> userRoles;
    List<Tag> tags;
    Map<String, List<BudgetScenario>> userWorkgroupsScenarios;

    public BudgetView(
        List<BudgetScenario> budgetScenarios,
        List<SectionGroupCost> sectionGroupCosts,
        List<SectionGroupCostComment> sectionGroupCostComments,
        List<SectionGroupCostInstructor> sectionGroupCostInstructors,
        List<ReasonCategory> reasonCategories,
        List<LineItem> lineItems,
        List<LineItemComment> lineItemComments,
        Budget budget,
        List<LineItemCategory> lineItemCategories,
        List<SectionGroup> sectionGroups,
        List<Section> sections,
        List<InstructorCost> instructorCosts,
        List<Instructor> activeInstructors,
        Set<Instructor> assignedInstructors,
        List<Course> courses,
        List<TeachingAssignment> teachingAssignments,
        List<SupportAssignment> supportAssignments,
        Set<User> users,
        List<InstructorTypeCost> instructorTypeCosts,
        List<InstructorType> instructorTypes,
        List<UserRole> userRoles,
        List<Tag> tags,
        Workgroup workgroup,
        Map<String, List<BudgetScenario>> userWorkgroupsScenarios) {
        setSectionGroups(sectionGroups);
        setSections(sections);
        setBudgetScenarios(budgetScenarios);
        setSectionGroupCosts(sectionGroupCosts);
        setLineItems(lineItems);
        setBudget(budget);
        setLineItemCategories(lineItemCategories);
        setInstructorCosts(instructorCosts);
        setSectionGroupCostComments(sectionGroupCostComments);
        setSectionGroupCostInstructors(sectionGroupCostInstructors);
        setReasonCategories(reasonCategories);
        setLineItemComments(lineItemComments);
        setCourses(courses);
        setTeachingAssignments(teachingAssignments);
        setSupportAssignments(supportAssignments);
        setUsers(users);
        setInstructorTypeCosts(instructorTypeCosts);
        setInstructorTypes(instructorTypes);
        setActiveInstructors(activeInstructors);
        setAssignedInstructors(assignedInstructors);
        setUserRoles(userRoles);
        setTags(tags);
        setWorkgroup(workgroup);
        setUserWorkgroupsScenarios(userWorkgroupsScenarios);
    }

    public Workgroup getWorkgroup() {
        return workgroup;
    }

    public void setWorkgroup(Workgroup workgroup ) {
        this.workgroup = workgroup;
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

    public List<Instructor> getActiveInstructors() {
        return activeInstructors;
    }

    public void setActiveInstructors(List<Instructor> activeInstructors) {
        this.activeInstructors = activeInstructors;
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

    public void setSectionGroupCostInstructors(List<SectionGroupCostInstructor> sectionGroupCostInstructors) {
        this.sectionGroupCostInstructors = sectionGroupCostInstructors;
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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public List<InstructorTypeCost> getInstructorTypeCosts() {
        return instructorTypeCosts;
    }

    public void setInstructorTypeCosts(List<InstructorTypeCost> instructorTypeCosts) {
        this.instructorTypeCosts = instructorTypeCosts;
    }

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public Set<Instructor> getAssignedInstructors() {
        return assignedInstructors;
    }

    public void setAssignedInstructors(Set<Instructor> assignedInstructors) {
        this.assignedInstructors = assignedInstructors;
    }

    public List<UserRole> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    public Map<String, List<BudgetScenario>> getUserWorkgroupsScenarios() {
        return userWorkgroupsScenarios;
    }

    public void setUserWorkgroupsScenarios(
        Map<String, List<BudgetScenario>> userWorkgroupsScenarios) {
        this.userWorkgroupsScenarios = userWorkgroupsScenarios;
    }

    public List<ReasonCategory> getReasonCategories() {
        return reasonCategories;
    }

    public void setReasonCategories(List<ReasonCategory> reasonCategories) {
        this.reasonCategories = reasonCategories;
    }
}
