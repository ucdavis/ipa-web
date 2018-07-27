package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.InstructorNote;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.BudgetService;
import edu.ucdavis.dss.ipa.services.InstructorNoteService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class InstructorNoteController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject InstructorNoteService instructorNoteService;

  @RequestMapping(value = "/api/schedules/{scheduleId}/instructors/{instructorId}/instructorNotes", method = RequestMethod.PUT, produces="application/json")
  @ResponseBody
  public InstructorNote createOrUpdateInstructorNote(@PathVariable long scheduleId,
                                                     @PathVariable long instructorId,
                                                     @RequestBody InstructorNote newInstructorNote,
                                                     HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findById(scheduleId);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer");

    InstructorNote originalInstructorNote = instructorNoteService.findOrCreateByScheduleIdAndInstructorId(scheduleId, instructorId);
    originalInstructorNote.setNote(newInstructorNote.getNote());

    return instructorNoteService.update(newInstructorNote);
  }
}
