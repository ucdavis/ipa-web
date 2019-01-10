package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroupCost;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.SectionGroupCostService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class SectionGroupCostController {
  @Inject SectionGroupCostService sectionGroupCostService;
  @Inject WorkgroupService workgroupService;
  @Inject SectionGroupService sectionGroupService;
  @Inject BudgetScenarioService budgetScenarioService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/sectionGroupCosts", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<SectionGroupCost> getSectionGroupCosts(@PathVariable long workgroupId,
                                                 @PathVariable long year,
                                                 HttpServletResponse httpResponse) {
    Workgroup workgroup = workgroupService.findOneById(workgroupId);

    if (workgroup == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
    List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findbyWorkgroupIdAndYear(workgroupId, year);

    return sectionGroupCosts;
  }

  @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/sectionGroups/{sectionGroupId}/sectionGroupCosts", method = RequestMethod.POST, produces="application/json")
  @ResponseBody
  public SectionGroupCost createSectionGroupCostComment(@PathVariable long budgetScenarioId,
                                                        @PathVariable long sectionGroupId,
                                                        HttpServletResponse httpResponse) {
    SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
    BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

    if (sectionGroup == null || budgetScenario == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    // Authorization check
    Long workGroupId = sectionGroup.getCourse().getSchedule().getWorkgroup().getId();
    authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

    return sectionGroupCostService.createFromSectionGroup(sectionGroup, budgetScenario);
  }

    @RequestMapping(value = "/api/budgetView/budgetScenarios/{budgetScenarioId}/sectionGroupCosts", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public SectionGroupCost createSectionGroupCost(@PathVariable long budgetScenarioId,
                                                          @RequestBody SectionGroupCost sectionGroupCost,
                                                          HttpServletResponse httpResponse) {
        BudgetScenario budgetScenario = budgetScenarioService.findById(budgetScenarioId);

        if (budgetScenario == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        // Authorization check
        Long workGroupId = budgetScenario.getBudget().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

        return sectionGroupCostService.createOrUpdateFrom(sectionGroupCost, budgetScenario);
    }
}
