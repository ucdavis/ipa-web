package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BudgetScenarioExcelView {
    Budget budget;
    BudgetScenario budgetScenario;
    Workgroup workgroup;
    List<SectionGroupCost> sectionGroupCosts;
    List<LineItem> lineItems;
    List<InstructorCost> instructorCosts;
    List<TeachingAssignment> teachingAssignments;
    List<InstructorType> instructorTypes;
    List<InstructorTypeCost> instructorTypeCosts;
    List<Instructor> activeInstructors;
    Set<User> users;
    Map<String, Map<String, Map<String, Long>>> censusMap;

    public BudgetScenarioExcelView(Budget budget,
                                   BudgetScenario budgetScenario,
                                   Workgroup workgroup,
                                   List<SectionGroupCost> sectionGroupCosts,
                                   List<LineItem> lineItems,
                                   List<InstructorCost> instructorCosts,
                                   List<TeachingAssignment> teachingAssignments,
                                   List<InstructorType> instructorTypes,
                                   List<InstructorTypeCost> instructorTypeCosts,
                                   List<Instructor> activeInstructors,
                                   Set<User> users,
                                   Map<String, Map<String, Map<String, Long>>> censusMap) {
        this.budget = budget;
        this.budgetScenario = budgetScenario;
        this.workgroup = workgroup;
        this.sectionGroupCosts = sectionGroupCosts;
        this.lineItems = lineItems;
        this.instructorCosts = instructorCosts;
        this.teachingAssignments = teachingAssignments;
        this.instructorTypes = instructorTypes;
        this.instructorTypeCosts = instructorTypeCosts;
        this.activeInstructors = activeInstructors;
        this.users = users;
        this.censusMap = censusMap;
    }

    public Budget getBudget() {
        return budget;
    }

    public void setBudget(Budget budget) {
        this.budget = budget;
    }

    public BudgetScenario getBudgetScenario() {
        return budgetScenario;
    }

    public void setBudgetScenario(BudgetScenario budgetScenario) {
        this.budgetScenario = budgetScenario;
    }

    public Workgroup getWorkgroup() {
        return workgroup;
    }

    public void setWorkgroup(Workgroup workgroup) {
        this.workgroup = workgroup;
    }

    public List<SectionGroupCost> getSectionGroupCosts() {
        return sectionGroupCosts;
    }

    public void setSectionGroupCosts(
        List<SectionGroupCost> sectionGroupCosts) {
        this.sectionGroupCosts = sectionGroupCosts;
    }

    public List<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(List<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public List<InstructorCost> getInstructorCosts() {
        return instructorCosts;
    }

    public void setInstructorCosts(
        List<InstructorCost> instructorCosts) {
        this.instructorCosts = instructorCosts;
    }

    public List<TeachingAssignment> getTeachingAssignments() {
        return teachingAssignments;
    }

    public void setTeachingAssignments(
        List<TeachingAssignment> teachingAssignments) {
        this.teachingAssignments = teachingAssignments;
    }

    public List<InstructorType> getInstructorTypes() {
        return instructorTypes;
    }

    public void setInstructorTypes(
        List<InstructorType> instructorTypes) {
        this.instructorTypes = instructorTypes;
    }

    public List<InstructorTypeCost> getInstructorTypeCosts() {
        return instructorTypeCosts;
    }

    public void setInstructorTypeCosts(
        List<InstructorTypeCost> instructorTypeCosts) {
        this.instructorTypeCosts = instructorTypeCosts;
    }

    public List<Instructor> getActiveInstructors() {
        return activeInstructors;
    }

    public void setActiveInstructors(
        List<Instructor> activeInstructors) {
        this.activeInstructors = activeInstructors;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Map<String, Map<String, Map<String, Long>>> getCensusMap() {
        return censusMap;
    }

    public void setCensusMap(Map<String, Map<String, Map<String, Long>>> censusMap) {
        this.censusMap = censusMap;
    }
}
