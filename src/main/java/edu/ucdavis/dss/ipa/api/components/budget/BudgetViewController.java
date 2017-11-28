package edu.ucdavis.dss.ipa.api.components.budget;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetScenarioView;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class BudgetViewController {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject InstructorCostService instructorCostService;
    @Inject UserService userService;
    @Inject SectionGroupCostCommentService sectionGroupCostCommentService;
    @Inject LineItemCommentService lineItemCommentService;
    @Inject Authorizer authorizer;

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
                                                   @RequestBody BudgetScenario budgetScenarioDTO,
                                                   HttpServletResponse httpResponse) {
        // Ensure valid params
        Budget budget = budgetService.findById(budgetId);

        if (budget == null || budgetScenarioDTO.getName() == null || budgetScenarioDTO.getName().length() == 0) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        BudgetScenario budgetScenario = null;

        // If a budget scenario id was supplied, copy data, else create from schedule
        if (scenarioId != null && scenarioId != 0) {
            budgetScenario = budgetScenarioService.createFromExisting(scenarioId, budgetScenarioDTO.getName());
        } else {
            budgetScenario = budgetScenarioService.findOrCreate(budget, budgetScenarioDTO.getName());
        }

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        return budgetViewFactory.createBudgetScenarioView(budgetScenario);
    }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long deleteBudgetScenario(@PathVariable long budgetScenarioId,
                                               HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
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
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
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

    @RequestMapping(value = "/api/budgetView/lineItems/{lineItemId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Long deleteLineItem(@PathVariable long lineItemId,
                                     HttpServletResponse httpResponse) {
        // Ensure valid params
        LineItem lineItem = lineItemService.findById(lineItemId);

        if (lineItem == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
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

        if (budgetScenario == null || lineItem == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return lineItemService.update(lineItemDTO);
    }


    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/lineItems", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public List<Integer> deleteLineItems(@PathVariable long budgetScenarioId,
                                      @RequestBody List<Integer> lineItemIds,
                                      HttpServletResponse httpResponse) {
        // Ensure valid params
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
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
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budget.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return budgetService.update(budgetDTO);
    }

    @RequestMapping(value = "/api/budgetView/instructorCosts/{instructorCostId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public InstructorCost updateInstructorCost(@PathVariable long instructorCostId,
                               @RequestBody InstructorCost instructorCostDTO,
                               HttpServletResponse httpResponse) {
        // Ensure valid params
        InstructorCost originalInstructorCost = instructorCostService.findById(instructorCostId);

        if (originalInstructorCost == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = originalInstructorCost.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return instructorCostService.update(instructorCostDTO);
    }

    @RequestMapping(value = "/api/budgetView/sectionGroupCosts/{sectionGroupCostId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public SectionGroupCost updateSectionGroupCost(@PathVariable long sectionGroupCostId,
                               @RequestBody SectionGroupCost sectionGroupCostDTO,
                               HttpServletResponse httpResponse) {
        // Ensure valid params
        SectionGroupCost originalSectionGroupCost = sectionGroupCostService.findById(sectionGroupCostId);

        if (originalSectionGroupCost == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
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
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = sectionGroupCost.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        sectionGroupCostCommentDTO.setUser(user);
        sectionGroupCostCommentDTO.setSectionGroupCost(sectionGroupCost);

        SectionGroupCostComment sectionGroupCostComment = sectionGroupCostCommentService.create(sectionGroupCostCommentDTO);

        if (sectionGroupCostComment == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
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
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorization check
        Long workGroupId = lineItem.getBudgetScenario().getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        lineItemCommentDTO.setUser(user);
        lineItemCommentDTO.setLineItem(lineItem);

        LineItemComment lineItemComment = lineItemCommentService.create(lineItemCommentDTO);

        if (lineItemComment == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        return lineItemComment;
    }
}
