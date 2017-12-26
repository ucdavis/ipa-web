package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JpaBudgetViewFactory implements BudgetViewFactory {
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject SectionGroupService sectionGroupService;
    @Inject CourseService courseService;
    @Inject SectionService sectionService;
    @Inject InstructorCostService instructorCostService;
    @Inject WorkgroupService workgroupService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupCostCommentService sectionGroupCostCommentService;
    @Inject LineItemCommentService lineItemCommentService;
    @Inject ScheduleService scheduleService;
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject UserService userService;

    @Override
    public BudgetView createBudgetView(long workgroupId, long year, Budget budget) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<BudgetScenario> budgetScenarios = budget.getBudgetScenarios();
        List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findByBudgetId(budget.getId());
        List<LineItem> lineItems = lineItemService.findByBudgetId(budget.getId());
        List<LineItemCategory> lineItemCategories = lineItemCategoryService.findAll();
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SectionGroup> sectionGroups = sectionGroupService.findByCourses(courses);
        List<InstructorCost> instructorCosts = instructorCostService.findOrCreateManyFromWorkgroupAndYear(workgroup, year);
        List<Instructor> instructors = instructorService.findByInstructorCosts(instructorCosts);
        List<SectionGroupCostComment> sectionGroupCostComments = sectionGroupCostCommentService.findBySectionGroupCosts(sectionGroupCosts);
        List<LineItemComment> lineItemComments = lineItemCommentService.findByLineItems(lineItems);
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleId(schedule.getId());
        List<SupportAssignment> supportAssignments = supportAssignmentService.findBySectionGroups(sectionGroups);

        Set<User> users = new HashSet<> (userService.findAllByWorkgroupAndRoleToken(workgroup, "academicPlanner"));
        Set<User> lineItemUsers = new HashSet<> (userService.findAllByLineItems(lineItems));
        users.addAll(lineItemUsers);

        BudgetView budgetView = new BudgetView(
                budgetScenarios,
                sectionGroupCosts,
                sectionGroupCostComments,
                lineItems,
                lineItemComments,
                budget,
                lineItemCategories,
                sectionGroups,
                sections,
                instructorCosts,
                instructors,
                courses,
                teachingAssignments,
                supportAssignments,
                users);

        return budgetView;
    }

    public BudgetScenarioView createBudgetScenarioView(BudgetScenario budgetScenario) {
        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts();
        List<LineItem> lineItems = budgetScenario.getLineItems();
        List<SectionGroupCostComment> sectionGroupCostComments = sectionGroupCostCommentService.findBySectionGroupCosts(sectionGroupCosts);
        List<LineItemComment> lineItemComments = lineItemCommentService.findByLineItems(lineItems);

        BudgetScenarioView budgetScenarioView = new BudgetScenarioView(budgetScenario, sectionGroupCosts, sectionGroupCostComments, lineItems, lineItemComments);

        return budgetScenarioView;
    }
}
