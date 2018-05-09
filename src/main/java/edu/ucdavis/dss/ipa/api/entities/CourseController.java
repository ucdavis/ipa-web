package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.security.Authorizer;
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
public class CourseController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/schedules/{scheduleId}/courses", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<Course> getInstructorTypes(@PathVariable long scheduleId,
                                         HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findById(scheduleId);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;

    }
    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    return schedule.getCourses();
  }
}
