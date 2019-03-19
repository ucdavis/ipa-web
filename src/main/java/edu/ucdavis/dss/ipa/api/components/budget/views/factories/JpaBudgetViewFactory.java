package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorCost;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.InstructorTypeCost;
import edu.ucdavis.dss.ipa.entities.LineItem;
import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import edu.ucdavis.dss.ipa.entities.LineItemComment;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.SectionGroupCostComment;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorCostService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeCostService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import edu.ucdavis.dss.ipa.services.LineItemCommentService;
import edu.ucdavis.dss.ipa.services.LineItemService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostCommentService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.SupportAssignmentService;
import edu.ucdavis.dss.ipa.services.TagService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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
    @Inject InstructorTypeCostService instructorTypeCostService;
    @Inject InstructorTypeService instructorTypeService;
    @Inject UserRoleService userRoleService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject TagService tagService;

    @Override
    public BudgetView createBudgetView(long workgroupId, long year, Budget budget) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);
        List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findByBudgetId(budget.getId());
        List<LineItem> lineItems = lineItemService.findByBudgetId(budget.getId());
        List<LineItemCategory> lineItemCategories = lineItemCategoryService.findAll();
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SectionGroup> sectionGroups = sectionGroupService.findByCourses(courses);
        List<InstructorCost> instructorCosts = budget.getInstructorCosts();
        List<InstructorTypeCost> instructorTypeCosts = instructorTypeCostService.findByBudgetId(budget.getId());
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroupId);
        Set<Instructor> assignedInstructors = new HashSet<> (instructorService.findAssignedByScheduleId(schedule.getId()));
        Set<Instructor> budgetedInstructors = new HashSet<> (instructorService.findBySectionGroupCosts(sectionGroupCosts));
        assignedInstructors.addAll(budgetedInstructors);

        List<SectionGroupCostComment> sectionGroupCostComments = sectionGroupCostCommentService.findBySectionGroupCosts(sectionGroupCosts);
        List<LineItemComment> lineItemComments = lineItemCommentService.findByLineItems(lineItems);
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleId(schedule.getId());
        List<SupportAssignment> supportAssignments = supportAssignmentService.findBySectionGroups(sectionGroups);
        List<Tag> tags = tagService.findByWorkgroupId(workgroupId);

        List<UserRole> userRoles = userRoleService.findByWorkgroup(workgroup);
        Set<User> users = new HashSet<> (userService.findAllByWorkgroup(workgroup));
        Set<User> lineItemUsers = new HashSet<> (userService.findAllByLineItems(lineItems));
        Set<User> teachingAssignmentUsers = new HashSet<>(userService.findAllByTeachingAssignments(teachingAssignments));

        users.addAll(lineItemUsers);
        users.addAll(teachingAssignmentUsers);

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
                activeInstructors,
                assignedInstructors,
                courses,
                teachingAssignments,
                supportAssignments,
                users,
                instructorTypeCosts,
                instructorTypes,
                userRoles,
                tags);

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
