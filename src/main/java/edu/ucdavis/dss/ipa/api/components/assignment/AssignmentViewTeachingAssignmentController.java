package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
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

        // When an academicCoordinator unapproves a teachingAssignment made by an academicCoordinator, delete instead of updating
        if (teachingAssignment.isApproved() == false && originalTeachingAssignment.isFromInstructor() == false) {
            teachingAssignmentService.delete( Long.valueOf(originalTeachingAssignment.getId()) );
            return null;
        }

        originalTeachingAssignment.setApproved(teachingAssignment.isApproved());

        return teachingAssignmentService.save(originalTeachingAssignment);
    }

    /**
     * Creates a variable number of teachingAssignments for the specified preference parameters
     * @param teachingAssignment
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/assignmentView/preferences/{scheduleId}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public List<TeachingAssignment> addPreference(@PathVariable long scheduleId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {
        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "federationInstructor", "senateInstructor");

        SectionGroup DTOsectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
        Course DTOcourse = courseService.getOneById(DTOsectionGroup.getCourse().getId());
        Instructor instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();

        //  Making a single teaching Preference if its a buyout/sab/release
        if (teachingAssignment.isSabbatical() || teachingAssignment.isCourseRelease() || teachingAssignment.isBuyout()) {
            // TODO: make one teachingAssignment
        } else if (DTOsectionGroup != null && DTOcourse != null) {
            // Find courses that match this courseNumber, subjectCode, scheduleId
            List<Course> courses = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(DTOcourse.getSubjectCode(), DTOcourse.getCourseNumber(), DTOcourse.getSchedule().getId());

            for (Course slotCourse : courses) {
                String slotSequencePattern = slotCourse.getSequencePattern();

                for (SectionGroup slotSectionGroup : slotCourse.getSectionGroups()) {

                    // Find associated sectiongroups tied to that course that match the term
                    if (slotSectionGroup.getTermCode().equals(teachingAssignment.getTermCode()) ) {
                        TeachingAssignment slotTeachingAssignment = new TeachingAssignment();

                        // Create a teachingAssignment for each sectionGroup
                        slotTeachingAssignment.setTermCode(teachingAssignment.getTermCode());
                        slotTeachingAssignment.setSectionGroup(slotSectionGroup);
                        slotTeachingAssignment.setApproved(false);
                        slotTeachingAssignment.setSchedule(slotCourse.getSchedule());
                        slotTeachingAssignment.setInstructor(instructor);

                        teachingAssignments.add(teachingAssignmentService.save(slotTeachingAssignment));
                    }
                }
            }
        }

        return teachingAssignments;
    }
}
