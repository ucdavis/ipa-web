package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class CourseController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject CourseService courseService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/courses", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<Course> getCourses(@PathVariable long workgroupId,
                                         @PathVariable long year,
                                         HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    return schedule.getCourses();
  }

  @RequestMapping(value = "/api/courses/{courseId}", method = RequestMethod.DELETE, produces="application/json")
  @ResponseBody
  public void deleteCourse(@PathVariable long courseId, HttpServletResponse httpResponse) {
    Course course = courseService.getOneById(courseId);

    if (course == null) {
      httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
      return;
    }

    Workgroup workgroup = course.getSchedule().getWorkgroup();
    authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

    courseService.delete(courseId);
  }

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/courses", method = RequestMethod.POST, produces="application/json")
  @ResponseBody
  public Course addCourse(@RequestBody @Validated Course course, @PathVariable Long workgroupId, @PathVariable Long year, HttpServletResponse httpResponse) {
    authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

    Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

    course.setSchedule(schedule);
    Course newCourse = courseService.create(course);

    if (newCourse != null) {
      return newCourse;
    } else {
      httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
      return null;
    }
  }

  @RequestMapping(value = "/api/courses/{courseId}", method = RequestMethod.PUT, produces="application/json")
  @ResponseBody
  public Course updateCourse(@PathVariable long courseId, @RequestBody @Validated Course courseDTO) {
    Course course = courseService.getOneById(courseId);
    Workgroup workgroup = course.getSchedule().getWorkgroup();
    authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

    return courseService.update(courseDTO);
  }
}
