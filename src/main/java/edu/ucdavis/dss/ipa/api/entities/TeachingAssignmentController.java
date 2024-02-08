package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class TeachingAssignmentController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject TeachingAssignmentService teachingAssignmentService;
  @Inject InstructorService instructorService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/teachingAssignments", method = RequestMethod.GET,produces="application/json")
  @ResponseBody
  public List<TeachingAssignment> getApprovedTeachingAssignments(@PathVariable long workgroupId,
                                                         @PathVariable long year,
                                                         @RequestParam(value = "instructorId", required = false) Long instructorId,
                                                         HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    Instructor instructor = instructorId != null ? instructorService.getOneById(instructorId) : null;

    if (instructorId != null && instructor == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    if (instructor != null) {
      return teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructorId);
    } else {
      return teachingAssignmentService.findApprovedByWorkgroupIdAndYear(workgroupId, year);
    }
  }

  @RequestMapping(value = "/api/teachingAssignments/{teachingAssignmentId}", method = RequestMethod.PUT, produces="application/json")
  @ResponseBody
  public TeachingAssignment updateTeachingAssignment(@PathVariable long teachingAssignmentId,
                                                     @RequestBody TeachingAssignment newTeachingAssignment,
                                                     HttpServletResponse httpResponse) {
    // Ensure valid params
    TeachingAssignment originalTeachingAssignment = teachingAssignmentService.findOneById(teachingAssignmentId);

    if (originalTeachingAssignment == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    // Authorization check
    Long workGroupId = originalTeachingAssignment.getSchedule().getWorkgroup().getId();
    authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "reviewer");

    return teachingAssignmentService.update(newTeachingAssignment);
  }
}
