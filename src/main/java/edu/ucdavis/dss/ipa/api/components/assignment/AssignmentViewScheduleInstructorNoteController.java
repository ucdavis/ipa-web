package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
@CrossOrigin
public class AssignmentViewScheduleInstructorNoteController {
    @Inject ScheduleService scheduleService;
    @Inject InstructorService instructorService;
    @Inject ScheduleInstructorNoteService scheduleInstructorNoteService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/assignmentView/scheduleInstructorNotes/{instructorId}/{workgroupId}/{year}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public ScheduleInstructorNote addScheduleInstructorNote(@PathVariable long instructorId, @PathVariable long workgroupId, @PathVariable long year, @RequestBody ScheduleInstructorNote scheduleInstructorNote) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Instructor instructor = instructorService.getOneById(instructorId);
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        scheduleInstructorNote.setInstructor(instructor);
        scheduleInstructorNote.setSchedule(schedule);

        return scheduleInstructorNoteService.saveScheduleInstructorNote(scheduleInstructorNote);
    }

    @RequestMapping(value = "/api/assignmentView/scheduleInstructorNotes/{scheduleInstructorNoteId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public ScheduleInstructorNote updateScheduleInstructorNote(@PathVariable long scheduleInstructorNoteId, @RequestBody ScheduleInstructorNote scheduleInstructorNote) {
        ScheduleInstructorNote originalScheduleInstructorNote = scheduleInstructorNoteService.findById(scheduleInstructorNoteId);
        Workgroup workgroup = originalScheduleInstructorNote.getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        originalScheduleInstructorNote.setInstructorComment(scheduleInstructorNote.getInstructorComment());
        originalScheduleInstructorNote.setAssignmentsCompleted(scheduleInstructorNote.getAssignmentsCompleted());

        return scheduleInstructorNoteService.saveScheduleInstructorNote(originalScheduleInstructorNote);
    }

}
