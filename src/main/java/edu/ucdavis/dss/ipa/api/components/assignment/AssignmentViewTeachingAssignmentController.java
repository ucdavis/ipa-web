package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class AssignmentViewTeachingAssignmentController {
    @Inject CurrentUser currentUser;
    @Inject AuthenticationService authenticationService;
    @Inject WorkgroupService workgroupService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;

    @RequestMapping(value = "/api/assignmentView/teachingAssignments", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingAssignment addTeachingAssignment(@RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {

        // Ensure teachingAssignment has either a buyout/release/sabbatical OR a SectionGroup/Instructor pair
        if (teachingAssignment.isBuyout() == false
            && teachingAssignment.isCourseRelease() == false
            && teachingAssignment.isSabbatical() == false
            && (teachingAssignment.getSectionGroup() == null || teachingAssignment.getInstructor() == null)) {
            return null;
        }

        SectionGroup sectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
        Instructor instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());

        Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // If a Teaching Assignment already exists, update it instead.
        TeachingAssignment existingTeachingAssignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);

        if (existingTeachingAssignment != null && existingTeachingAssignment.getId() >= 0) {
            existingTeachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());
            existingTeachingAssignment.setInstructor(instructor);

            existingTeachingAssignment.setApproved(teachingAssignment.isApproved());

            return teachingAssignmentService.save(existingTeachingAssignment);
        }

        teachingAssignment.setSectionGroup(sectionGroup);
        teachingAssignment.setInstructor(instructor);
        teachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());

        return teachingAssignmentService.save(teachingAssignment);
    }

    @RequestMapping(value = "/api/assignmentView/teachingAssignments/{teachingAssignmentId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingAssignment updateTeachingAssignment(@PathVariable long teachingAssignmentId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {
        TeachingAssignment originalTeachingAssignment = teachingAssignmentService.findOneById(teachingAssignmentId);
        Workgroup workgroup = originalTeachingAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        originalTeachingAssignment.setApproved(teachingAssignment.isApproved());

        return teachingAssignmentService.save(originalTeachingAssignment);
    }

}
