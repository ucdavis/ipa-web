package edu.ucdavis.dss.ipa.api.components.budget.views.factories;

import static edu.ucdavis.dss.ipa.entities.enums.TermDescription.*;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetComparisonExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.BudgetSummary;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
    @Inject TermService termService;
    @Inject BudgetCalculationService budgetCalculationService;
    @Inject Authorization authorization;
    @Inject BudgetScenarioRepository budgetScenarioRepository;
    @Inject SectionGroupCostInstructorService sectionGroupCostInstructorService;

    @Override
    public BudgetView createBudgetView(long workgroupId, long year, Budget budget) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        User currentUser = userService.getOneByLoginId(authorization.getLoginId());
        List<Workgroup> userWorkgroups = currentUser.getWorkgroups();
        Map<String, List<BudgetScenario>> userWorkgroupsScenarios = new HashMap<>();

        for (Workgroup userWorkgroup : userWorkgroups) {
            userWorkgroupsScenarios.put(userWorkgroup.getName(), budgetScenarioRepository.findbyWorkgroupIdAndYear(userWorkgroup.getId(), year));
        }

        List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);
        List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findByBudgetId(budget.getId());
        List<LineItem> lineItems = lineItemService.findByBudgetId(budget.getId());
        List<LineItemCategory> lineItemCategories = lineItemCategoryService.findAll();
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SectionGroup> sectionGroups = sectionGroupService.findByCourses(courses);
        List<InstructorCost> instructorCosts = budget.getInstructorCosts();
        List<InstructorTypeCost> instructorTypeCosts = instructorTypeCostService.findByBudgetId(budget.getId());

        // fetch any additional costs from budgetScenario snapshots
        for (BudgetScenario budgetScenario : budgetScenarios) {
            instructorCosts.addAll(budgetScenario.getInstructorCosts());
            instructorTypeCosts.addAll(budgetScenario.getInstructorTypeCosts());
        }

        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroupId);
        Set<Instructor> assignedInstructors = new HashSet<> (instructorService.findAssignedByScheduleId(schedule.getId()));
        Set<Instructor> budgetedInstructors = new HashSet<> (instructorService.findBySectionGroupCosts(sectionGroupCosts));
        assignedInstructors.addAll(budgetedInstructors);

        List<SectionGroupCostComment> sectionGroupCostComments = sectionGroupCostCommentService.findBySectionGroupCosts(sectionGroupCosts);
        List<SectionGroupCostInstructor> sectionGroupCostInstructors = sectionGroupCostInstructorService.findByBudgetId(budget.getId());
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
                sectionGroupCostInstructors,
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
                workgroup,
                userWorkgroupsScenarios);

        return budgetView;
    }

    public BudgetScenarioView createBudgetScenarioView(BudgetScenario budgetScenario) {
        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts();
        List<LineItem> lineItems = budgetScenario.getLineItems();
        List<SectionGroupCostComment> sectionGroupCostComments = sectionGroupCostCommentService.findBySectionGroupCosts(sectionGroupCosts);
        List<SectionGroupCostInstructor> sectionGroupCostInstructors = sectionGroupCostInstructorService.findByBudgetScenarioId(budgetScenario.getId());
        List<LineItemComment> lineItemComments = lineItemCommentService.findByLineItems(lineItems);
        List<InstructorCost> instructorCosts = instructorCostService.findByBudgetScenarioId(budgetScenario.getId());
        List<InstructorTypeCost> instructorTypeCosts = instructorTypeCostService.findByBudgetScenarioId(budgetScenario.getId());

        BudgetScenarioView budgetScenarioView = new BudgetScenarioView(budgetScenario, sectionGroupCosts, sectionGroupCostComments, sectionGroupCostInstructors, lineItems, lineItemComments, instructorCosts, instructorTypeCosts);

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
        return createBudgetScenarioExcelView(budgetScenarioDTO, true);
    };

    /**
     *
     * @param budgetScenarioDTO expects budgetScenarioId
     * @param includeCensus default is true
     * @return
     */
    public BudgetScenarioExcelView createBudgetScenarioExcelView(BudgetScenario budgetScenarioDTO, Boolean includeCensus) {
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioDTO.getId());
        Budget budget = budgetScenario.getBudget();
        List<String> budgetScenarioTermCodes = new ArrayList<>();
        for(String termCodeShort : Arrays.asList(FALL.getShortTermCode(), WINTER.getShortTermCode(), SPRING.getShortTermCode())){
            String termCode = termService.getTermCodeFromYearAndTerm(budgetScenario.getBudget().getSchedule().getYear(), termCodeShort);
            budgetScenarioTermCodes.add(termCode);
        }
        Workgroup workgroup = budgetScenario.getBudget().getSchedule().getWorkgroup();
        List<SectionGroupCost> sectionGroupCosts = budgetScenario.getSectionGroupCosts().stream().filter(sgc -> (sgc.isDisabled() == false && budgetScenarioTermCodes.contains(sgc.getTermCode()))).collect(Collectors.toList());
        List<LineItem> lineItems = budgetScenario.getLineItems().stream().filter(li -> li.getHidden() == false).collect(Collectors.toList());
        List<TeachingAssignment> teachingAssignments = budget.getSchedule().getTeachingAssignments();
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        List<InstructorCost> instructorCosts = budgetScenario.getIsSnapshot() ? budgetScenario.getInstructorCosts() : budget.getInstructorCosts();
        List<InstructorTypeCost> instructorTypeCosts = budgetScenario.getIsSnapshot() ? budgetScenario.getInstructorTypeCosts() : budget.getInstructorTypeCosts();
        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroup.getId());
        Set<User> users = new HashSet<> (userService.findAllByWorkgroup(workgroup));
        Set<User> lineItemUsers = new HashSet<> (userService.findAllByLineItems(lineItems));
        Set<User> teachingAssignmentUsers = new HashSet<>(userService.findAllByTeachingAssignments(teachingAssignments));
        users.addAll(lineItemUsers);
        users.addAll(teachingAssignmentUsers);

        List<String> budgetScenarioSubjectCodes = sectionGroupCosts.stream().map(SectionGroupCost::getSubjectCode).distinct().collect(Collectors.toList());

        List<DwCensus> censusList = new ArrayList<>();
        for (String budgetScenarioSubjectCode : budgetScenarioSubjectCodes) {
            for (String budgetScenarioTermCode : budgetScenarioTermCodes) {
                censusList.addAll(dwRepository.getCensusBySubjectCodeAndTermCode(budgetScenarioSubjectCode, budgetScenarioTermCode).stream().filter(c -> "CURRENT".equals(c.getSnapshotCode())).collect(Collectors.toList()));
            }
        }

        Map<String, Map<String, Map<String, Long>>> censusMap = new HashMap<>(new HashMap<>());
        /* {
            termCode: {
              subj+crse: {
                    sequence: long
              }
            }
        } */

        if (includeCensus == true) {
            for (DwCensus census : censusList) {
                String termCode = census.getTermCode();
                String sequencePattern = census.getSequencePattern();
                String courseIdentifier = census.getSubjectCode() + census.getCourseNumber();

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
        }


        // Calculate totals
        Map<String, Map<BudgetSummary, BigDecimal>> termTotals = budgetCalculationService.calculateTermTotals(budget, budgetScenario, sectionGroupCosts, budgetScenarioTermCodes, workgroup, lineItems);

        BudgetScenarioExcelView budgetScenarioExcelView = new BudgetScenarioExcelView(budget, budgetScenario, workgroup, sectionGroupCosts, lineItems, instructorCosts, teachingAssignments, instructorTypes, instructorTypeCosts, activeInstructors, users, censusMap, budgetScenarioTermCodes, termTotals);

        return budgetScenarioExcelView;
    }

    public BudgetComparisonExcelView createBudgetComparisonExcelView(List<List<BudgetScenario>> budgetComparisonList) {
        // [[s1, s2], [s1, s2], [s1,s2]]
        List<List<BudgetScenarioExcelView>> budgetScenarioExcelViewPairList = new ArrayList<>();

        for (List<BudgetScenario> budgetComparison : budgetComparisonList) {
            List<BudgetScenarioExcelView> budgetScenarioExcelViewPair = Arrays.asList(
                createBudgetScenarioExcelView(budgetComparison.get(0), false),
                createBudgetScenarioExcelView(budgetComparison.get(1), false)
            );

            budgetScenarioExcelViewPairList.add(budgetScenarioExcelViewPair);
        }

        return new BudgetComparisonExcelView(budgetScenarioExcelViewPairList);
    }
}
