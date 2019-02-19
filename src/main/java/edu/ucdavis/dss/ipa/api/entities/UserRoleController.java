package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
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
public class UserRoleController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject UserRoleService userRoleService;
  @Inject WorkgroupService workgroupService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/userRoles", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<UserRole> getUserRoles(@PathVariable long workgroupId,
                                         HttpServletResponse httpResponse) {
    Workgroup workgroup = workgroupService.findOneById(workgroupId);

    if (workgroup == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    return userRoleService.findByWorkgroup(workgroup);
  }
}
