package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin
public class BudgetController {
  @Inject BudgetService budgetService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/budget", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public Budget getBudget(@PathVariable long workgroupId,
                                 @PathVariable long year,
                                 HttpServletResponse httpResponse) {
    Budget budget = budgetService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

    if (budget == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(budget.getSchedule().getWorkgroup().getId(), "academicPlanner", "reviewer");

    return budget;
  }
}
