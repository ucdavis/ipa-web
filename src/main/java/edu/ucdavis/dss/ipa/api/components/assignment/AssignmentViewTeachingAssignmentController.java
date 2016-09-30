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

    @RequestMapping(value = "/api/assignmentView/schedules/{scheduleId}/teachingAssignments", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingAssignment addTeachingAssignment(@PathVariable long scheduleId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {

        // Ensure teachingAssignment has either a buyout/release/sabbatical OR a SectionGroup/Instructor pair
        if (teachingAssignment.isBuyout() == false
            && teachingAssignment.isCourseRelease() == false
            && teachingAssignment.isSabbatical() == false
            && (teachingAssignment.getSectionGroup() == null || teachingAssignment.getInstructor() == null)) {
            return null;
        }

        SectionGroup sectionGroup = null;
        Long sectionGroupId = -1L;

        if (teachingAssignment.getSectionGroup() != null) {
            sectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
            sectionGroupId = sectionGroup.getId();
        }

        Instructor instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());
        Schedule schedule = scheduleService.findById(scheduleId);

        Workgroup workgroup = schedule.getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // If a Teaching Assignment already exists, update it instead.
        TeachingAssignment existingTeachingAssignment = teachingAssignmentService.findBySectionGroupIdAndInstructorIdAndScheduleIdAndTermCodeAndBuyoutAndCourseReleaseAndSabbatical(
                sectionGroupId, instructor.getId(), scheduleId, teachingAssignment.getTermCode(), teachingAssignment.isBuyout(), teachingAssignment.isCourseRelease(), teachingAssignment.isSabbatical());

        if (existingTeachingAssignment != null && existingTeachingAssignment.getId() >= 0) {
            existingTeachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());
            existingTeachingAssignment.setInstructor(instructor);

            existingTeachingAssignment.setApproved(teachingAssignment.isApproved());

            return teachingAssignmentService.save(existingTeachingAssignment);
        }

        // Handle non sectionGroup based preference
        if (teachingAssignment.isBuyout() == true
                || teachingAssignment.isCourseRelease() == true
                || teachingAssignment.isSabbatical() == true) {

            teachingAssignment.setInstructor(instructor);
            teachingAssignment.setSchedule(schedule);
        } else {
            teachingAssignment.setSectionGroup(sectionGroup);
            teachingAssignment.setInstructor(instructor);
            teachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());
        }

        return teachingAssignmentService.save(teachingAssignment);
    }

    @RequestMapping(value = "/api/assignmentView/teachingAssignments/{teachingAssignmentId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingAssignment updateTeachingAssignment(@PathVariable long teachingAssignmentId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {
        TeachingAssignment originalTeachingAssignment = teachingAssignmentService.findOneById(teachingAssignmentId);
        Schedule schedule = scheduleService.findById(originalTeachingAssignment.getSchedule().getId());
        Workgroup workgroup = schedule.getWorkgroup();
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
     * Removes a variable number of teachingAssignments for the specified preference parameters.
     * @param teachingAssignmentId
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/assignmentView/preferences/{teachingAssignmentId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public List<TeachingAssignment> removePreference(@PathVariable long teachingAssignmentId, HttpServletResponse httpResponse) {
        TeachingAssignment DTOteachingAssignment = teachingAssignmentService.findOneById(teachingAssignmentId);
        Workgroup workgroup = DTOteachingAssignment.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRoles(workgroup.getId(), "senateInstructor", "federationInstructor");

        Instructor DTOinstructor = DTOteachingAssignment.getInstructor();

        List<Long> teachingAssignmentIdsToDelete = new ArrayList<Long>();
        List<TeachingAssignment> teachingAssignmentsToDelete = new ArrayList<TeachingAssignment>();

        // Delete a preference tied to 1 to many sectionGroups
        if (DTOteachingAssignment.getSectionGroup() != null) {
            SectionGroup DTOsectionGroup = DTOteachingAssignment.getSectionGroup();
            Course DTOcourse = DTOsectionGroup.getCourse();

            // Find any other courses that match this pattern
            List<Course> courses = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(DTOcourse.getSubjectCode(), DTOcourse.getCourseNumber(), DTOcourse.getSchedule().getId());

            boolean oneIsApproved = false;

            // Find all relevant teachingAssignments
            // If at least one of the teachingAssignments is approved, do nothing
            for (Course slotCourse : courses) {
                for (SectionGroup slotSectionGroup : slotCourse.getSectionGroups()) {
                    for (TeachingAssignment slotTeachingAssignment : slotSectionGroup.getTeachingAssignments()) {
                        // Looking for teachingAssignments from the relevant instructor
                        if (slotTeachingAssignment.getInstructor().getId() == DTOinstructor.getId()
                                && slotTeachingAssignment.getTermCode().equals(DTOteachingAssignment.getTermCode())) {
                            teachingAssignmentIdsToDelete.add(slotTeachingAssignment.getId());
                            teachingAssignmentsToDelete.add((slotTeachingAssignment));

                            if (slotTeachingAssignment.isApproved()) {
                                oneIsApproved = true;
                                break;
                            }
                        }
                    }
                }
            }

            // Do nothing, the preference UI should not have allowed deletion as an option - this preference has already been approved.
            if (oneIsApproved == true) {
                return null;
            } else {
                for (Long slotTeachingAssignmentId : teachingAssignmentIdsToDelete) {
                    teachingAssignmentService.delete(slotTeachingAssignmentId);
                }
            }

        }
        // Delete a course release / sabbatical / buyout
        else {
            teachingAssignmentsToDelete.add(DTOteachingAssignment);
            teachingAssignmentService.delete((teachingAssignmentId));

        }

        return teachingAssignmentsToDelete;
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
        Schedule schedule = scheduleService.findById(scheduleId);
        Workgroup workgroup = schedule.getWorkgroup();

        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "federationInstructor", "senateInstructor");

        SectionGroup DTOsectionGroup = null;
        Course DTOcourse = null;

        if (teachingAssignment.getSectionGroup() != null) {
            DTOsectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
            DTOcourse = courseService.getOneById(DTOsectionGroup.getCourse().getId());
        }

        Instructor instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();

        //  Making a single teaching Preference if its a buyout/sab/release
        if (teachingAssignment.isSabbatical() || teachingAssignment.isCourseRelease() || teachingAssignment.isBuyout()) {

            teachingAssignment.setApproved(false);
            teachingAssignment.setSchedule(schedule);
            teachingAssignment.setInstructor(instructor);

            Integer priority = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId()).size() + 1;
            teachingAssignment.setPriority(priority);

            teachingAssignments.add(teachingAssignmentService.save(teachingAssignment));
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

                        Integer priority = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId()).size() + 1;
                        slotTeachingAssignment.setPriority(priority);
                        teachingAssignments.add(teachingAssignmentService.save(slotTeachingAssignment));
                    }
                }
            }
        }

        return teachingAssignments;
    }

    @RequestMapping(value = "/api/assignmentView/schedules/{scheduleId}/teachingAssignments", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public List<Long> updatePreferenceOrder(@PathVariable long scheduleId, @RequestBody List<Long> sortedTeachingPreferenceIds, HttpServletResponse httpResponse) {
        Schedule schedule = scheduleService.findById(scheduleId);
        Workgroup workgroup = schedule.getWorkgroup();

        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "federationInstructor", "senateInstructor");

        Integer priority = 1;

        for(Long id : sortedTeachingPreferenceIds) {
            TeachingAssignment teachingAssignment = teachingAssignmentService.findOneById(id);

            teachingAssignment.setPriority(priority);
            teachingAssignmentService.save(teachingAssignment);
            priority++;
        }

        return sortedTeachingPreferenceIds;
    }
}
