package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.InstructorService;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin
public class InstructorController {
  @Inject ScheduleService scheduleService;
  @Inject InstructorService instructorService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/instructors", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public Set<Instructor> getInstructors(@PathVariable long workgroupId,
                                        @PathVariable long year,
                                         HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;

    }
    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    Set<Instructor> instructors = new HashSet<>();
    Set<Instructor> activeInstructors = new HashSet<>(instructorService.findActiveByWorkgroupId(schedule.getWorkgroup().getId()));
    Set<Instructor> assignedInstructors = new HashSet<> (instructorService.findAssignedByScheduleId(schedule.getId()));

    instructors.addAll(assignedInstructors);
    instructors.addAll(activeInstructors);

    return instructors;
  }
}
