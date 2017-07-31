package edu.ucdavis.dss.ipa.api.components.budget;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetView;
import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.JpaAnnualViewFactory;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.entities.validation.CourseValidator;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Time;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class BudgetViewController {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    @Inject LineItemService lineItemService;
    @Inject LineItemCategoryService lineItemCategoryService;
    @Inject SectionGroupCostService sectionGroupCostService;
    @Inject InstructorCostService instructorCostService;

    /**
     * Delivers the JSON payload for the Courses View (nee Annual View), used on page load.
     *
     * @param workgroupId
     * @param year
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/budgetView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public BudgetView showBudgetView(@PathVariable long workgroupId,
                                     @PathVariable long year,
                                     HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        // Ensure budget exists
        Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

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
    public BudgetScenario createBudgetScenario(@PathVariable long budgetId,
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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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

        return budgetScenario;
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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return lineItemService.update(lineItemDTO);
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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

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
        Authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return sectionGroupCostService.update(sectionGroupCostDTO);
    }
}
