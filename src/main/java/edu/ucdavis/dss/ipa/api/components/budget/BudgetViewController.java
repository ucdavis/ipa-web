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

    @RequestMapping(value = "/api/budgetView/budgetScenarios/budgets/{budgetId}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public BudgetScenario createBudgetScenario(@PathVariable long budgetId,
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

        BudgetScenario budgetScenario = budgetScenarioService.findOrCreate(budget, budgetScenarioDTO.getName());

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
}
