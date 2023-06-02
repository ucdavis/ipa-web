package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class BudgetScenarioController {
  @Inject BudgetScenarioService budgetScenarioService;
  @Inject BudgetScenarioRepository budgetScenarioRepository;
  @Inject UserService userService;
  @Inject WorkgroupService workgroupService;

  @Inject Authorizer authorizer;
  @Inject Authorization authorization;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/budgetScenarios", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<BudgetScenario> getBudgetScenarios(@PathVariable long workgroupId,
                                                 @PathVariable long year,
                                                 HttpServletResponse httpResponse) {
    Workgroup workgroup = workgroupService.findOneById(workgroupId);

    if (workgroup == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
    List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);

    return budgetScenarios;
  }

  @RequestMapping(value = "/api/workgroups/{workgroupId}/budgetScenarios", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<Long> getAllBudgetScenarios(@PathVariable long workgroupId,
                                          HttpServletResponse httpResponse) {
    Workgroup workgroup = workgroupService.findOneById(workgroupId);

    if (workgroup == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
    List<BudgetScenario> budgetScenarios = budgetScenarioService.findByWorkgroupId(workgroupId);

    return budgetScenarios.stream().map(s -> s.getBudget().getSchedule().getYear()).distinct().collect(Collectors.toList());
  }

  /**
   *
   * @param year
   * @param httpResponse
   * @return
   *      {
   *        workgroup: {
   *          current: [],
   *          previous: []
   *        }
   *      }
   */
  @RequestMapping(value = "/api/years/{year}/budgetScenarios", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public Map<String, Map<String, List<BudgetScenario>>> getDepartmentBudgetComparisonScenariosByYear(@PathVariable long year, HttpServletResponse httpResponse) {
    User currentUser = userService.getOneByLoginId(authorization.getLoginId());
    List<Workgroup> userWorkgroups = currentUser.getWorkgroups();
    Map<String, Map<String, List<BudgetScenario>>> departmentBudgetComparisonScenarios = new HashMap<>();

    for (Workgroup userWorkgroup : userWorkgroups) {
      List<BudgetScenario> currentWorkgroupScenarios =
          budgetScenarioRepository.findbyWorkgroupIdAndYear(userWorkgroup.getId(), year);
      List<BudgetScenario> previousWorkgroupScenarios =
          budgetScenarioRepository.findbyWorkgroupIdAndYear(userWorkgroup.getId(), year - 1);
      departmentBudgetComparisonScenarios
          .put(userWorkgroup.getName(), new HashMap<String, List<BudgetScenario>>() {
            {
              put("current", currentWorkgroupScenarios);
              put("previous", previousWorkgroupScenarios);
            }
          });
    }

    return departmentBudgetComparisonScenarios;
  }
}
