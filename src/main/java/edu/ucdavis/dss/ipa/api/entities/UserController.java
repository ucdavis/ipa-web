package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
public class UserController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject UserService userService;
  @Inject WorkgroupService workgroupService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/users", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public Set<User> getSectionGroups(@PathVariable long workgroupId,
                                    @PathVariable long year,
                                    HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
    Workgroup workgroup = workgroupService.findOneById(workgroupId);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    Set<User> users = new HashSet<>();

    Set<User> usersWithInstructorRole = new HashSet<>(userService.findAllByWorkgroup(workgroup));
    Set<User> assignedUsers = new HashSet<>(userService.findAllByTeachingAssignments(schedule.getTeachingAssignments()));

    users.addAll(usersWithInstructorRole);
    users.addAll(assignedUsers);

    return users;
  }
}
