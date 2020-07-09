package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import static edu.ucdavis.dss.ipa.api.helpers.Utilities.isNumeric;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioExcelView;
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
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.method.P;
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
    @Inject DataWarehouseRepository dwRepository;

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
                tags,
                workgroup);

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

    public BudgetExcelView createBudgetExcelView(List<BudgetScenario> budgetScenarios) {
        List<BudgetScenarioExcelView> budgetScenarioExcelViews = new ArrayList<>();

        for (BudgetScenario budgetScenario : budgetScenarios) {
            BudgetScenarioExcelView budgetScenarioExcelView = this.createBudgetScenarioExcelView(budgetScenario);
            budgetScenarioExcelViews.add(budgetScenarioExcelView);
        }

        return new BudgetExcelView(budgetScenarioExcelViews);
    }

    public BudgetScenarioExcelView createBudgetScenarioExcelView(BudgetScenario budgetScenarioDTO) {
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioDTO.getId());
        Budget budget = budgetScenario.getBudget();
        Workgroup workgroup = budgetScenario.getBudget().getSchedule().getWorkgroup();
        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts().stream().filter(sgc -> sgc.isDisabled() == false).collect(Collectors.toList());
        List<LineItem> lineItems = budgetScenario.getLineItems().stream().filter(li -> li.getHidden() == false).collect(Collectors.toList());
        List<InstructorCost> instructorCosts = budget.getInstructorCosts();
        List<TeachingAssignment> teachingAssignments = budget.getSchedule().getTeachingAssignments();
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        List<InstructorTypeCost> instructorTypeCosts = instructorTypeCostService.findByBudgetId(budget.getId());
        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroup.getId());
        Set<User> users = new HashSet<> (userService.findAllByWorkgroup(workgroup));
        Set<User> lineItemUsers = new HashSet<> (userService.findAllByLineItems(lineItems));
        Set<User> teachingAssignmentUsers = new HashSet<>(userService.findAllByTeachingAssignments(teachingAssignments));
        users.addAll(lineItemUsers);
        users.addAll(teachingAssignmentUsers);

        List<String> budgetScenarioSubjectCodes = sectionGroupCosts.stream().map(SectionGroupCost::getSubjectCode).distinct().collect(Collectors.toList());
        List<String> budgetScenarioTermCodes = Arrays.asList("201905", "202007", "202010", "202001", "202003");

        List<DwCensus> censusList = new ArrayList<>();
        for (String budgetScenarioSubjectCode : budgetScenarioSubjectCodes) {
            List<DwCensus> subjectCodeCensus = dwRepository.getCensusBySubjectCodeAndTermCode(budgetScenarioSubjectCode, "201910").stream().filter(c -> c.getSnapshotCode().equals("CURRENT")).collect(
            Collectors.toList());

            censusList.addAll(subjectCodeCensus);

        }

        Map<String, Map<String, Map<String, Long>>> censusMap = new HashMap<>(new HashMap<>());
        for (DwCensus census : censusList) {
            String termCode = census.getTermCode();
            String sequencePattern;

            if (isNumeric(census.getSequenceNumber())) {
                sequencePattern = census.getSequenceNumber();
            } else {
                sequencePattern = String.valueOf(census.getSequenceNumber().charAt(0));
            }

            String courseIdentifier = census.getSubjectCode() + census.getCourseNumber();
//            {
//                termCode: {
//                  subj+crse: {
//                        sequence: long
//                  }
//                }
//            }

            if (censusMap.get(termCode) == null) {
                censusMap.put(termCode, new HashMap<>());
            }

            if (censusMap.get(termCode).get(courseIdentifier) == null) {
                censusMap.get(termCode).put(courseIdentifier, new HashMap<>());
            }

            if (censusMap.get(termCode).get(courseIdentifier).get(sequencePattern) == null) {
                censusMap.get(termCode).get(courseIdentifier)
                    .put(sequencePattern, census.getCurrentEnrolledCount());
            } else {
                censusMap.get(termCode).get(courseIdentifier).put(sequencePattern, censusMap.get(termCode).get(courseIdentifier).get(sequencePattern) + census.getCurrentEnrolledCount());
            }
        }

        // calculate sectionGroupCost
        /*for (SectionGroupCost slotCost : sectionGroupCosts) {

            if (slotCost.getCost() == null) {
                // check if instructor has salary else check if category cost
                if (slotCost.getInstructor() != null) {
                    InstructorCost instructorCost = instructorCostService
                        .findByInstructorIdAndBudgetId(slotCost.getInstructor().getId(),
                            budget.getId());

                    if (instructorCost == null) {
                        // check for category cost
                        List<InstructorTypeCost> potentialCosts = instructorTypeCostService.findByBudgetId(slotCost.getBudgetScenario().getBudget().getId());
                        slotCost.setCost(BigDecimal.ZERO);
                        for(InstructorTypeCost potentialCost : potentialCosts){
                            if(potentialCost.getInstructorType().getId() == slotCost.getInstructorType().getId()){
                                slotCost.setCost(BigDecimal.valueOf(potentialCost.getCost()));
                            }
                        }
                    } else {
                        System.err.println(
                            "Found cost, cost was " + instructorCost.getCost() + " Id is " +
                                instructorCost.getId() + " Section group cost ID was " +
                                slotCost.getId());
                        slotCost.setCost(instructorCost.getCost());
                    }
                }
            }

            //System.err.println("Cost" + slotCost.getCost() + slotCost.getId());
        }*/

        BudgetScenarioExcelView budgetScenarioExcelView = new BudgetScenarioExcelView(budget, budgetScenario, workgroup, sectionGroupCosts, lineItems, instructorCosts, teachingAssignments, instructorTypes, instructorTypeCosts, activeInstructors, users, censusMap);

        return budgetScenarioExcelView;
    }
}
