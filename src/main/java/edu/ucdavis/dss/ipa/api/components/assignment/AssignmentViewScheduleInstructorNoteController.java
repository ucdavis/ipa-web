package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
@CrossOrigin
public class AssignmentViewScheduleInstructorNoteController {
    @Inject
    CurrentUser currentUser;
    @Inject
    AuthenticationService authenticationService;
    @Inject
    WorkgroupService workgroupService;
    @Inject
    ScheduleService scheduleService;
    @Inject
    CourseService courseService;
    @Inject
    TeachingAssignmentService teachingAssignmentService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;
    @Inject ScheduleInstructorNoteService scheduleInstructorNoteService;

    @RequestMapping(value = "/api/assignmentView/scheduleInstructorNotes/{instructorId}/{workgroupId}/{year}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public ScheduleInstructorNote addScheduleInstructorNote(@PathVariable long instructorId, @PathVariable long workgroupId, @PathVariable long year, @RequestBody ScheduleInstructorNote scheduleInstructorNote, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Instructor instructor = instructorService.getOneById(instructorId);
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        scheduleInstructorNote.setInstructor(instructor);
        scheduleInstructorNote.setSchedule(schedule);

        return scheduleInstructorNoteService.saveScheduleInstructorNote(scheduleInstructorNote);
    }

    @RequestMapping(value = "/api/assignmentView/scheduleInstructorNotes/{scheduleInstructorNoteId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public ScheduleInstructorNote updateScheduleInstructorNote(@PathVariable long scheduleInstructorNoteId, @RequestBody ScheduleInstructorNote scheduleInstructorNote, HttpServletResponse httpResponse) {
        ScheduleInstructorNote originalScheduleInstructorNote = scheduleInstructorNoteService.findById(scheduleInstructorNoteId);
        Workgroup workgroup = originalScheduleInstructorNote.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        originalScheduleInstructorNote.setInstructorComment(scheduleInstructorNote.getInstructorComment());
        originalScheduleInstructorNote.setAssignmentsCompleted(scheduleInstructorNote.getAssignmentsCompleted());

        return scheduleInstructorNoteService.saveScheduleInstructorNote(originalScheduleInstructorNote);
    }

}
