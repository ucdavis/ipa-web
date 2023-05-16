package edu.ucdavis.dss.ipa.api.components.budget;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetComparisonExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.api.components.budget.views.WorkgroupIdBudgetScenarioId;
import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.services.*;
import java.util.stream.Collectors;
import javax.sound.sampled.Line;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BudgetViewController {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject SectionGroupCostInstructorService sectionGroupCostInstructorService;
    @Inject InstructorCostService instructorCostService;
    @Inject UserService userService;
    @Inject SectionGroupCostCommentService sectionGroupCostCommentService;
    @Inject LineItemCommentService lineItemCommentService;
    @Inject Authorizer authorizer;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorTypeCostService instructorTypeCostService;
    @Inject InstructorService instructorService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject InstructorTypeService instructorTypeService;
    @Inject ExpenseItemService expenseItemService;
    @Inject ExpenseItemTypeService expenseItemTypeService;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    /**
     * Delivers the JSON payload for the Courses View (nee Annual View), used on page load.
     *
     * @param workgroupId
     * @param year
     * @return
     */
    @RequestMapping(value = "/api/budgetView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public BudgetView showBudgetView(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        // Ensure budget exists
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Ensure at least one scenario exists
        if (budget.getBudgetScenarios().size() == 0) {

            BudgetScenario budgetScenario = budgetScenarioService.findOrCreate(budget, "Default Scenario");
            List<BudgetScenario> scenarios = new ArrayList<>();
            scenarios.add(budgetScenario);
            budget.setBudgetScenarios(scenarios);
        }

        return budgetViewFactory.createBudgetView(workgroupId, year, budget);
    }

    /**
     * @param budgetId
     * @param budgetScenarioDTO
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/budgetView/budgets/{budgetId}/budgetScenarios", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public BudgetScenarioView createBudgetScenario(@PathVariable long budgetId,
                                                   @RequestParam(value="scenarioId", required = false) Long scenarioId,
                                                   @RequestParam(value = "copyFunds", required = false) boolean copyFunds,
                                                   @RequestBody BudgetScenario budgetScenarioDTO,
                                                   HttpServletResponse httpResponse) {
        // Ensure valid params
        Budget budget = budgetService.findById(budgetId);

        if (budget == null || budgetScenarioDTO.getName() == null || budgetScenarioDTO.getName().length() == 0) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        BudgetScenario budgetScenario = null;

        // If a budget scenario id was supplied, copy data, else create from schedule
        if (scenarioId != null && scenarioId != 0) {
            budgetScenario = budgetScenarioService.createFromExisting(workGroupId, scenarioId, budgetScenarioDTO.getName(), copyFunds);
        } else {
            budgetScenario = budgetScenarioService.findOrCreate(budget, budgetScenarioDTO.getName());
        }

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        return budgetViewFactory.createBudgetScenarioView(budgetScenario);
    }

    @RequestMapping(value = "/api/budgetView/budgets/{budgetId}/budgetScenarios/{budgetScenarioId}/budgetRequest", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public BudgetScenarioView createBudgetRequestScenario(@PathVariable long budgetId,
                                                          @PathVariable long budgetScenarioId,
                                                          HttpServletResponse httpResponse) {

        Budget budget = budgetService.findById(budgetId);
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budget == null || budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        BudgetScenario budgetRequestScenario = budgetScenarioService.createBudgetRequestScenario(workGroupId, budgetScenarioId);

        return budgetViewFactory.createBudgetScenarioView(budgetRequestScenario);
    };

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long deleteBudgetScenario(@PathVariable long budgetScenarioId,
                                     HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        budgetScenarioService.deleteById(budgetScenarioId);

        return budgetScenarioId;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/lineItems", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public LineItem createLineItem(@PathVariable long budgetScenarioId,
                                   @RequestBody LineItem lineItemDTO,
                                   HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);
        LineItemCategory lineItemCategory = lineItemCategoryService.findById(lineItemDTO.getLineItemCategoryId());

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        if (lineItemDTO.getTeachingAssignment() != null) {
            TeachingAssignment teachingAssignment = teachingAssignmentService.findOneById(lineItemDTO.getTeachingAssignment().getId());
            lineItemDTO.setTeachingAssignment(teachingAssignment);
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        // Build lineItem
        lineItemDTO.setBudgetScenario(budgetScenario);
        lineItemDTO.setLineItemCategory(lineItemCategory);
        LineItem lineItem = lineItemService.findOrCreate(lineItemDTO);

        return lineItem;
    }

    @RequestMapping(value = "/api/budgetView/budgets/{budgetId}/instructorTypeCosts", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public InstructorTypeCost createInstructorTypeCost(@PathVariable long budgetId,
                                                       @RequestBody InstructorTypeCost newInstructorTypeCost,
                                                       HttpServletResponse httpResponse) {
        // Ensure valid params
        Budget budget = budgetService.findById(budgetId);
        InstructorType instructorType = null;

        if (newInstructorTypeCost.getInstructorType() != null) {
            instructorType = instructorTypeService.findById(newInstructorTypeCost.getInstructorType().getId());
        }

        if (budget == null || instructorType == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        newInstructorTypeCost.setBudget(budget);
        newInstructorTypeCost.setInstructorType(instructorType);
        InstructorTypeCost instructorTypeCost = instructorTypeCostService.findOrCreate(newInstructorTypeCost);

        return instructorTypeCost;
    }

    @RequestMapping(value = "/api/budgetView/lineItems/{lineItemId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long deleteLineItem(@PathVariable long lineItemId,
                               HttpServletResponse httpResponse) {
        // Ensure valid params
        LineItem lineItem = lineItemService.findById(lineItemId);

        if (lineItem == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = lineItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        lineItemService.deleteById(lineItemId);

        return lineItemId;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/lineItems/{lineItemId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public LineItem updateLineItem(@PathVariable long budgetScenarioId,
                                   @PathVariable long lineItemId,
                                   @RequestBody LineItem lineItemDTO,
                                   HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);
        LineItem lineItem = lineItemService.findById(lineItemDTO.getId());

        if (budgetScenario == null || lineItem == null || lineItem.getLineItemCategory() == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        LineItemCategory lineItemCategory = lineItemCategoryService.findById(lineItemDTO.getLineItemCategory().getId());

        if (lineItemCategory == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        } else {
            lineItemDTO.setLineItemCategory(lineItemCategory);
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return lineItemService.update(lineItemDTO);
    }

    @RequestMapping(value = "/api/budgetView/instructorTypeCosts/{instructorTypeId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public InstructorTypeCost updateInstructorTypeCost(@PathVariable long instructorTypeId,
                                                       @RequestBody InstructorTypeCost newInstructorTypeCost,
                                                       HttpServletResponse httpResponse) {
        // Ensure valid params
        InstructorTypeCost originalInstructorTypeCost = instructorTypeCostService.findById(instructorTypeId);

        if (originalInstructorTypeCost == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = originalInstructorTypeCost.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return instructorTypeCostService.update(newInstructorTypeCost);
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/lineItems/lock", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<LineItem> updateLineItems(@PathVariable long budgetScenarioId,
                                      @RequestBody List<Long> lineItemIds,
                                      HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        List<LineItem> lineItems = lineItemIds.stream().map(id -> lineItemService.findById(id)).collect(Collectors.toList());

        lineItems.forEach(lineItem -> lineItem.setLocked(true));

        return lineItemService.update(lineItems);
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/lineItems", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<Long> deleteLineItems(@PathVariable long budgetScenarioId,
                                      @RequestBody List<Long> lineItemIds,
                                      HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        lineItemService.deleteMany(lineItemIds);

        return lineItemIds;
    }

    @RequestMapping(value = "/api/budgetView/budgets/{budgetId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Budget updateBudget(@PathVariable long budgetId,
                               @RequestBody Budget budgetDTO,
                               HttpServletResponse httpResponse) {
        // Ensure valid params
        Budget budget = budgetService.findById(budgetId);

        if (budget == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return budgetService.update(budgetDTO);
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public BudgetScenario updateBudgetScenario(@PathVariable long budgetScenarioId,
                                               @RequestBody BudgetScenario newBudgetScenario,
                                               HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        budgetScenario.setName(newBudgetScenario.getName());
        budgetScenario.setActiveTermsBlob(newBudgetScenario.getActiveTermsBlob());

        return budgetScenarioService.update(budgetScenario);
    }

    @RequestMapping(value = "/api/budgetView/instructorCosts/{instructorCostId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public InstructorCost updateInstructorCost(@PathVariable long instructorCostId,
                                               @RequestBody InstructorCost instructorCostDTO,
                                               HttpServletResponse httpResponse) {
        // Ensure valid params
        InstructorCost originalInstructorCost = instructorCostService.findById(instructorCostId);

        if (originalInstructorCost == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = originalInstructorCost.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        InstructorTypeCost instructorTypeCost = null;

        if (instructorCostDTO.getInstructorTypeCost() != null) {
            instructorTypeCost = instructorTypeCostService.findById(instructorCostDTO.getInstructorTypeCost().getId());
        }

        instructorCostDTO.setInstructorTypeCost(instructorTypeCost);

        return instructorCostService.update(instructorCostDTO);
    }

    @RequestMapping(value = "/api/budgetView/budgets/{budgetId}/instructorCosts", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public InstructorCost createInstructorCost(@PathVariable long budgetId,
                                               @RequestBody InstructorCost instructorCostDTO,
                                               HttpServletResponse httpResponse) {
        // Ensure valid params
        Budget budget = budgetService.findById(budgetId);
        Instructor instructor = instructorService.getOneById(instructorCostDTO.getInstructor().getId());

        if (budget == null || instructor == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        instructorCostDTO.setBudget(budget);
        instructorCostDTO.setInstructor(instructor);

        return instructorCostService.findOrCreate(instructorCostDTO);
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public SectionGroupCost updateSectionGroupCost(@PathVariable long sectionGroupCostId,
                                                   @RequestBody SectionGroupCost sectionGroupCostDTO,
                                                   HttpServletResponse httpResponse) {
        // Ensure valid params
        SectionGroupCost originalSectionGroupCost = sectionGroupCostService.findById(sectionGroupCostId);

        if (originalSectionGroupCost == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = originalSectionGroupCost.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return sectionGroupCostService.update(sectionGroupCostDTO);
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}/sectionGroupCostComments", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public SectionGroupCostComment createSectionGroupCostComment(@PathVariable long sectionGroupCostId,
                                                                 @RequestBody SectionGroupCostComment sectionGroupCostCommentDTO,
                                                                 HttpServletResponse httpResponse) {
        // Ensure valid params
        SectionGroupCost sectionGroupCost = sectionGroupCostService.findById(sectionGroupCostId);
        User user = userService.getOneByLoginId(sectionGroupCostCommentDTO.getUser().getLoginId());
        if (user == null || sectionGroupCost == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = sectionGroupCost.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        sectionGroupCostCommentDTO.setUser(user);
        sectionGroupCostCommentDTO.setSectionGroupCost(sectionGroupCost);

        SectionGroupCostComment sectionGroupCostComment = sectionGroupCostCommentService.create(sectionGroupCostCommentDTO);

        if (sectionGroupCostComment == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        return sectionGroupCostComment;
    }

    @RequestMapping(value = "/api/budgetView/lineItems/{lineItemId}/lineItemComments", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public LineItemComment createLineItemComment(@PathVariable long lineItemId,
                                                 @RequestBody LineItemComment lineItemCommentDTO,
                                                 HttpServletResponse httpResponse) {
        // Ensure valid params
        LineItem lineItem = lineItemService.findById(lineItemId);
        User user = userService.getOneByLoginId(lineItemCommentDTO.getUser().getLoginId());

        if (user == null || lineItem == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = lineItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        lineItemCommentDTO.setUser(user);
        lineItemCommentDTO.setLineItem(lineItem);

        LineItemComment lineItemComment = lineItemCommentService.create(lineItemCommentDTO);

        if (lineItemComment == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        return lineItemComment;
    }

    @RequestMapping(value = "/api/budgetView/downloadExcel", method = RequestMethod.POST)
    public View downloadAllDepartmentsExcel(@RequestBody List<BudgetScenario> budgetScenarioIds, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        return budgetViewFactory.createBudgetExcelView(budgetScenarioIds);
    }

    @RequestMapping(value = "/api/budgetView/downloadBudgetComparisonExcel", method = RequestMethod.POST)
    public BudgetComparisonExcelView downloadBudgetComparisonsExcel(@RequestBody List<List<BudgetScenario>>  budgetComparisonList, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        return budgetViewFactory.createBudgetComparisonExcelView(budgetComparisonList);
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}/sectionGroupCostInstructors", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<SectionGroupCostInstructor> addSectionGroupCostInstructor(@PathVariable long sectionGroupCostId, @RequestBody List<SectionGroupCostInstructor> sectionGroupCostInstructors) {
        Long workGroupId = sectionGroupCostService.findById(sectionGroupCostId).getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        List<SectionGroupCostInstructor> instructors = new ArrayList<>();
        for(SectionGroupCostInstructor sectionGroupCostInstructor : sectionGroupCostInstructors){
            sectionGroupCostInstructor.setSectionGroupCost(sectionGroupCostService.findById(sectionGroupCostId));
            instructors.add(sectionGroupCostInstructorService.findOrCreate(sectionGroupCostInstructor));
        }
        return instructors;
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}/sectionGroupCostInstructors/{sectionGroupCostInstructorId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public SectionGroupCostInstructor updateSectionGroupCost(@PathVariable long sectionGroupCostId,
                                                             @PathVariable long sectionGroupCostInstructorId,
                                                             @RequestBody SectionGroupCostInstructor sectionGroupCostInstructor,
                                                             HttpServletResponse httpResponse) {
        // Ensure valid params
        SectionGroupCostInstructor originalSectionGroupCostInstructor = sectionGroupCostInstructorService.findById(sectionGroupCostInstructorId);

        if (originalSectionGroupCostInstructor == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        sectionGroupCostInstructor.setInstructor(instructorService.getOneById(sectionGroupCostInstructor.getInstructor().getId()));
        sectionGroupCostInstructor.setSectionGroupCost(sectionGroupCostService.findById(sectionGroupCostId));
        sectionGroupCostInstructor.setInstructorType(instructorTypeService.findById(sectionGroupCostInstructor.getInstructorType().getId()));

        // Authorization check
        Long workGroupId = sectionGroupCostInstructor.getSectionGroupCost().getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return sectionGroupCostInstructorService.update(sectionGroupCostInstructor);
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}/sectionGroupCostInstructors/{sectionGroupCostInstructorId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteSectionGroupCostInstructor(@PathVariable long sectionGroupCostId, @PathVariable long sectionGroupCostInstructorId, HttpServletResponse httpResponse) {
        // Ensure valid params
        SectionGroupCostInstructor sectionGroupCostInstructor = sectionGroupCostInstructorService.findById(sectionGroupCostInstructorId);

        if (sectionGroupCostInstructor == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = sectionGroupCostInstructor.getSectionGroupCost().getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        sectionGroupCostInstructorService.delete(sectionGroupCostInstructorId);

        return sectionGroupCostInstructorId;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/expenseItems", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public ExpenseItem createExpenseItem(@PathVariable long budgetScenarioId,
                                         @RequestBody ExpenseItem expenseItemDTO,
                                         HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);
        ExpenseItemType expenseItemType = expenseItemTypeService.findById(expenseItemDTO.getExpenseItemType().getId());

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        // Build lineItem
        expenseItemDTO.setBudgetScenario(budgetScenario);
        expenseItemDTO.setExpenseItemType(expenseItemType);
        ExpenseItem expenseItem = expenseItemService.findOrCreate(expenseItemDTO);

        return expenseItem;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/expenseItems/{expenseItemId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public ExpenseItem updateExpenseItem(@PathVariable long budgetScenarioId,
                                         @PathVariable long expenseItemId,
                                         @RequestBody ExpenseItem expenseItemDTO,
                                         HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);
        ExpenseItem expenseItem = expenseItemService.findById(expenseItemDTO.getId());

        if (budgetScenario == null || expenseItem == null || expenseItem.getExpenseItemType() == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        ExpenseItemType expenseItemType = expenseItemTypeService.findById(expenseItemDTO.getExpenseItemType().getId());

        if (expenseItemType == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        } else {
            expenseItemDTO.setExpenseItemType(expenseItemType);
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return expenseItemService.update(expenseItemDTO);
    }

    @RequestMapping(value = "/api/budgetView/expenseItems/{expenseItemId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long deleteExpenseItem(@PathVariable long expenseItemId,
                                  HttpServletResponse httpResponse) {
        // Ensure valid params
        ExpenseItem expenseItem = expenseItemService.findById(expenseItemId);

        if (expenseItem == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = expenseItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        expenseItemService.deleteById(expenseItemId);

        return expenseItemId;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/expenseItems", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<Long> deleteExpenseItems(@PathVariable long budgetScenarioId,
                                         @RequestBody List<Long> expenseItemIds,
                                         HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        expenseItemService.deleteMany(expenseItemIds);

        return expenseItemIds;
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/expenseItems", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<ExpenseItem> getExpenseItems(@PathVariable long budgetScenarioId,
                                             HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");
        List<ExpenseItem> expenseItems = expenseItemService.findByBudgetScenarioId(budgetScenarioId);

        return expenseItems;
    }
}
