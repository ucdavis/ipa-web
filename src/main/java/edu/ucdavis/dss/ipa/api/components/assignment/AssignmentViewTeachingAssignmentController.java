package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.dw.dto.DwCourse;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@RestController
@CrossOrigin
public class AssignmentViewTeachingAssignmentController {
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;
    @Inject DataWarehouseRepository dwRepository;
    @Inject Authorizer authorizer;
    @Inject InstructorTypeService instructorTypeService;

    @RequestMapping(value = "/api/assignmentView/schedules/{scheduleId}/teachingAssignments", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingAssignment addTeachingAssignment(@PathVariable long scheduleId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {
        Schedule schedule = scheduleService.findById(scheduleId);

        if (schedule == null || teachingAssignment == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Instructor instructor = null;
        InstructorType instructorType = null;

        if (teachingAssignment.getInstructor() != null) {
            instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());
        }

        if (teachingAssignment.getInstructorType() != null) {
            instructorType = instructorTypeService.findById(teachingAssignment.getInstructorType().getId());
        }

        Workgroup workgroup = schedule.getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // Ensure valid params
        // Either:
        // 1) teachingAssignment is a buyout/release/sabbatical/in residence/work life balance/leave of absence
        // 2) teachingAssignment has a sectionGroup
        // 3) teachingAssignment is an instructorType assignment (no instructorId)

        if (instructor == null && instructorType != null) {
            teachingAssignment.setInstructorType(instructorType);
            teachingAssignment.setSchedule(schedule);

            return teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);
        }

        if (teachingAssignment.isBuyout() == false
            && teachingAssignment.isCourseRelease() == false
            && teachingAssignment.isSabbatical() == false
            && teachingAssignment.isInResidence() == false
            && teachingAssignment.isWorkLifeBalance() == false
            && teachingAssignment.isLeaveOfAbsence() == false
            && teachingAssignment.isSabbaticalInResidence() == false
            && (teachingAssignment.getSectionGroup() == null || teachingAssignment.getInstructor() == null)) {
            return null;
        }

        // Handle buyout/release/sabbatical/in-residence based preferences
        if (teachingAssignment.isBuyout() == true
                || teachingAssignment.isCourseRelease() == true
                || teachingAssignment.isInResidence() == true
                || teachingAssignment.isWorkLifeBalance() == true
                || teachingAssignment.isLeaveOfAbsence() == true
                || teachingAssignment.isSabbaticalInResidence() == true
                || teachingAssignment.isSabbatical() == true) {

            teachingAssignment.setInstructor(instructor);
            teachingAssignment.setSchedule(schedule);

            TeachingAssignment newTeachingAssignment = teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);

            return newTeachingAssignment;
        }

        // Handle sectionGroup based preferences
        // Get sectionGroupId
        Long sectionGroupId = -1L;
        SectionGroup sectionGroup = null;

        if (teachingAssignment.getSectionGroup() != null) {
            sectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
            sectionGroupId = sectionGroup.getId();
        }

        // If a Teaching Assignment already exists, update it instead.
        TeachingAssignment existingTeachingAssignment = teachingAssignmentService.findByTeachingAssignment(teachingAssignment);

        if (existingTeachingAssignment != null && existingTeachingAssignment.getId() >= 0) {
            existingTeachingAssignment.setSchedule(schedule);
            existingTeachingAssignment.setInstructor(instructor);

            existingTeachingAssignment.setApproved(teachingAssignment.isApproved());

            if (existingTeachingAssignment.isApproved() && sectionGroup != null) {
                sectionGroup.setShowTheStaff(false);
                sectionGroupService.save(sectionGroup);
            }

            return teachingAssignmentService.saveAndAddInstructorType(existingTeachingAssignment);
        }

        // Create a new Teaching Assignment
        teachingAssignment.setSectionGroup(sectionGroup);
        teachingAssignment.setInstructor(instructor);
        teachingAssignment.setSchedule(sectionGroup.getCourse().getSchedule());

        if (teachingAssignment.isApproved() && sectionGroup != null) {
            sectionGroup.setShowTheStaff(false);
            sectionGroupService.save(sectionGroup);
        }

        return teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);

    }

    @RequestMapping(value = "/api/assignmentView/teachingAssignments/{teachingAssignmentId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingAssignment updateTeachingAssignment(@PathVariable long teachingAssignmentId, @RequestBody TeachingAssignment teachingAssignment, HttpServletResponse httpResponse) {
        TeachingAssignment originalTeachingAssignment = teachingAssignmentService.findOneById(teachingAssignmentId);

        if (originalTeachingAssignment == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Schedule schedule = scheduleService.findById(originalTeachingAssignment.getSchedule().getId());
        Workgroup workgroup = schedule.getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // When an academicCoordinator unapproves a teachingAssignment made by an academicCoordinator, delete instead of updating
        if (teachingAssignment.isApproved() == false && originalTeachingAssignment.isFromInstructor() == false) {
            teachingAssignmentService.delete( Long.valueOf(originalTeachingAssignment.getId()) );
            return null;
        }

        // A teachingAssignment that suggested a new course (and/or sectionGroup) has been approved.
        if (teachingAssignment.isApproved() == true
                && teachingAssignment.getSuggestedEffectiveTermCode() != null
                && teachingAssignment.getSuggestedSubjectCode() != null
                && teachingAssignment.getSuggestedCourseNumber() != null) {


            SectionGroup sectionGroup = null;
            List<Course> coursesMatchingSuggestedCourse = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(teachingAssignment.getSuggestedSubjectCode(), teachingAssignment.getSuggestedCourseNumber(), schedule.getId());

            // Check if the course exists already, if not create it
            if (coursesMatchingSuggestedCourse.size() > 0) {
                for (Course slotCourse : coursesMatchingSuggestedCourse) {

                    // Check if the sectionGroup exists already
                    for (SectionGroup slotSectionGroup : slotCourse.getSectionGroups()) {
                        if (slotSectionGroup.getTermCode().equals(teachingAssignment.getTermCode())) {
                            sectionGroup = slotSectionGroup;
                        }
                    }

                    // Necessary sectionGroup did not exist, need to create it
                    if (sectionGroup == null) {
                        sectionGroup = new SectionGroup();
                        sectionGroup.setTermCode(teachingAssignment.getTermCode());
                        sectionGroup.setCourse(slotCourse);
                        sectionGroup = sectionGroupService.save(sectionGroup);
                    }
                }
            } else {
                // Necessary course was not found, need to create it
                Course course = new Course();

                course.setCourseNumber(teachingAssignment.getSuggestedCourseNumber());
                course.setSchedule(schedule);
                course.setEffectiveTermCode(teachingAssignment.getSuggestedEffectiveTermCode());
                course.setSubjectCode(teachingAssignment.getSuggestedSubjectCode());
                course.setSequencePattern("001");

                DwCourse dwCourse = dwRepository.findCourse(teachingAssignment.getSuggestedSubjectCode(), teachingAssignment.getSuggestedCourseNumber(), teachingAssignment.getSuggestedEffectiveTermCode());
                if (dwCourse != null && dwCourse.getTitle() != null) {
                    course.setTitle(dwCourse.getTitle());
                } else {
                    course.setTitle(teachingAssignment.getSuggestedSubjectCode() + " " + teachingAssignment.getSuggestedCourseNumber());
                }

                course.setUnitsHigh(dwCourse.getCreditHoursHigh());
                course.setUnitsLow(dwCourse.getCreditHoursLow());

                course = courseService.create(course);

                // Create a sectionGroup for the teachingAssignment

                sectionGroup = new SectionGroup();
                sectionGroup.setCourse(course);
                sectionGroup.setTermCode(teachingAssignment.getTermCode());

                sectionGroup = sectionGroupService.save(sectionGroup);
            }

            // Associate teachingAssignment to the newly created sectionGroup and remove the suggested course metadata
            originalTeachingAssignment.setSectionGroup(sectionGroup);
            originalTeachingAssignment.setSuggestedEffectiveTermCode(null);
            originalTeachingAssignment.setSuggestedSubjectCode(null);
            originalTeachingAssignment.setSuggestedCourseNumber(null);
            originalTeachingAssignment.setSuggestedTitle(null);
            originalTeachingAssignment.setApproved(teachingAssignment.isApproved());

            teachingAssignmentService.saveAndAddInstructorType(originalTeachingAssignment);

            originalTeachingAssignment.setSuggestedCourseNumber(teachingAssignment.getSuggestedCourseNumber());
            originalTeachingAssignment.setSuggestedSubjectCode(teachingAssignment.getSuggestedSubjectCode());
            originalTeachingAssignment.setSuggestedEffectiveTermCode(teachingAssignment.getSuggestedEffectiveTermCode());
            originalTeachingAssignment.setSuggestedTitle(teachingAssignment.getSuggestedTitle());

            return originalTeachingAssignment;
        }

        originalTeachingAssignment.setApproved(teachingAssignment.isApproved());

        if (originalTeachingAssignment.isApproved() && originalTeachingAssignment.getSectionGroup() != null) {
            SectionGroup sectionGroup = originalTeachingAssignment.getSectionGroup();
            sectionGroup.setShowTheStaff(false);
            sectionGroupService.save(sectionGroup);
        }

        return teachingAssignmentService.saveAndAddInstructorType(originalTeachingAssignment);
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
        TeachingAssignment teachingAssignmentDto = teachingAssignmentService.findOneById(teachingAssignmentId);

        if (teachingAssignmentDto == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Workgroup workgroup = teachingAssignmentDto.getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "instructor");

        Instructor instructorDto = teachingAssignmentDto.getInstructor();

        List<Long> teachingAssignmentIdsToDelete = new ArrayList<Long>();
        List<TeachingAssignment> teachingAssignmentsToDelete = new ArrayList<TeachingAssignment>();

        // Delete a preference tied to 1 to many sectionGroups
        if (teachingAssignmentDto.getSectionGroup() != null) {
            SectionGroup sectionGroupDto = teachingAssignmentDto.getSectionGroup();
            Course courseDto = sectionGroupDto.getCourse();

            // Find any other courses that match this pattern
            List<Course> courses = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(courseDto.getSubjectCode(), courseDto.getCourseNumber(), courseDto.getSchedule().getId());

            // Find all relevant teachingAssignments
            // If at least one of the teachingAssignments is approved, do nothing
            for (Course slotCourse : courses) {
                for (SectionGroup slotSectionGroup : slotCourse.getSectionGroups()) {
                    for (TeachingAssignment slotTeachingAssignment : slotSectionGroup.getTeachingAssignments()) {
                        Instructor slotTeachingAssignmentInstructor = slotTeachingAssignment.getInstructor();

                        // This has occurred in production. What does it mean?
                        if(slotTeachingAssignmentInstructor == null) { continue; }

                        // Looking for teachingAssignments from the relevant instructor
                        if (slotTeachingAssignmentInstructor.getId() == instructorDto.getId()
                        && slotTeachingAssignment.getTermCode().equals(teachingAssignmentDto.getTermCode())) {
                            teachingAssignmentIdsToDelete.add(slotTeachingAssignment.getId());
                            teachingAssignmentsToDelete.add(slotTeachingAssignment);
                        }
                    }
                }
            }

            if (teachingAssignmentDto.isFromInstructor() && teachingAssignmentDto.isApproved()) {
                for (Long slotTeachingAssignmentId : teachingAssignmentIdsToDelete) {
                    TeachingAssignment teachingAssignment = teachingAssignmentService.findOneById(slotTeachingAssignmentId);
                    teachingAssignment.setFromInstructor(false);
                    teachingAssignmentService.update(teachingAssignment);
                }
            } else {
                for (Long slotTeachingAssignmentId : teachingAssignmentIdsToDelete) {
                    teachingAssignmentService.delete(slotTeachingAssignmentId);
                }
            }
        }
        // Delete a course release / sabbatical / buyout
        else {
            if (teachingAssignmentDto.isFromInstructor() && teachingAssignmentDto.isApproved()) {
                teachingAssignmentDto.setFromInstructor(false);
                teachingAssignmentService.update(teachingAssignmentDto);
            } else {
                teachingAssignmentsToDelete.add(teachingAssignmentDto);
                teachingAssignmentService.delete(teachingAssignmentId);
            }
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

        if (schedule == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Workgroup workgroup = schedule.getWorkgroup();

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "instructor");

        SectionGroup DTOsectionGroup = null;
        Course DTOcourse = null;

        if (teachingAssignment.getSectionGroup() != null) {
            DTOsectionGroup = sectionGroupService.getOneById(teachingAssignment.getSectionGroup().getId());
            DTOcourse = courseService.getOneById(DTOsectionGroup.getCourse().getId());
        }

        Instructor instructor = instructorService.getOneById(teachingAssignment.getInstructor().getId());

        List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();

        // Make a single teaching Assignment if its based on a suggested course
        if (teachingAssignment.getSuggestedCourseNumber() != null
            && teachingAssignment.getSuggestedEffectiveTermCode() != null
            && teachingAssignment.getSuggestedSubjectCode() != null) {

            // Make sure suggested teaching Assignment doesn't already exist
            TeachingAssignment existingSuggestedTeachingAssignment = teachingAssignmentService.findByInstructorIdAndScheduleIdAndTermCodeAndSuggestedCourseNumberAndSuggestedSubjectCodeAndSuggestedEffectiveTermCode(
                    instructor.getId(),
                    schedule.getId(),
                    teachingAssignment.getTermCode(),
                    teachingAssignment.getSuggestedCourseNumber(),
                    teachingAssignment.getSuggestedSubjectCode(),
                    teachingAssignment.getSuggestedEffectiveTermCode());

            if (existingSuggestedTeachingAssignment != null) {
                teachingAssignments.add(existingSuggestedTeachingAssignment);
                return teachingAssignments;
            }

            // Does this course already exist in the schedule? if so, treat this as a normal teachingAssignment creation
            List<Course> existingSuggestedCourses = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(
                    teachingAssignment.getSuggestedSubjectCode(),
                    teachingAssignment.getSuggestedCourseNumber(),
                    schedule.getId());

            if (existingSuggestedCourses.size() > 0) {
                // Set the sectionGroup and course so teachingAssignment(s) can be made
                for (Course slotCourse : existingSuggestedCourses) {
                    for (SectionGroup slotSectionGroup : slotCourse.getSectionGroups()) {
                        if (slotSectionGroup.getTermCode().equals(teachingAssignment.getTermCode())) {
                            DTOsectionGroup = slotSectionGroup;
                            DTOcourse = slotCourse;
                        }
                    }
                }


            }

            // If attempts to match the suggested teachingAssignment to an existing sectionGroup failed, we should create a suggested teachingAssignment.
            if (DTOsectionGroup == null || DTOcourse == null){
                // Make a single suggested teaching Assignment
                teachingAssignment.setSchedule(schedule);
                teachingAssignment.setInstructor(instructor);
                teachingAssignment.setFromInstructor(true);
                TeachingAssignment maxPriorityTeachingAssignment = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId()).stream().max(Comparator.comparing(TeachingAssignment::getPriority)).get();
                Integer priority = maxPriorityTeachingAssignment.getPriority() + 1;
                teachingAssignment.setPriority(priority);
                teachingAssignments.add(teachingAssignmentService.saveAndAddInstructorType(teachingAssignment));
                return teachingAssignments;
            }

        }

        // Make a single teaching Preference if its a non sectiongroup assignment
        if (teachingAssignment.isSabbatical() || teachingAssignment.isCourseRelease() || teachingAssignment.isBuyout() || teachingAssignment.isInResidence() || teachingAssignment.isWorkLifeBalance() || teachingAssignment.isLeaveOfAbsence() || teachingAssignment.isSabbaticalInResidence()) {
            teachingAssignment.setApproved(false);
            teachingAssignment.setSchedule(schedule);
            teachingAssignment.setInstructor(instructor);
            teachingAssignment.setFromInstructor(true);

            List<TeachingAssignment> currentAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId());
            Integer currentMaxPriority = currentAssignments.size() > 0 ? currentAssignments.stream().max(Comparator.comparing(TeachingAssignment::getPriority)).get().getPriority() : 0;

            Integer priority = currentMaxPriority + 1;
            teachingAssignment.setPriority(priority);

            teachingAssignments.add(teachingAssignmentService.saveAndAddInstructorType(teachingAssignment));
        } else if (DTOsectionGroup != null && DTOcourse != null) {
            // Find courses that match this courseNumber, subjectCode, scheduleId
            List<Course> courses = courseService.findBySubjectCodeAndCourseNumberAndScheduleId(DTOcourse.getSubjectCode(), DTOcourse.getCourseNumber(), DTOcourse.getSchedule().getId());

            for (Course slotCourse : courses) {
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
                        slotTeachingAssignment.setFromInstructor(true);

                        TeachingAssignment maxPriorityTeachingAssignment = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId()).stream().max(Comparator.comparing(TeachingAssignment::getPriority)).get();
                        Integer priority = maxPriorityTeachingAssignment.getPriority() + 1;
                        slotTeachingAssignment.setPriority(priority);
                        teachingAssignments.add(teachingAssignmentService.saveAndAddInstructorType(slotTeachingAssignment));
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

        if (schedule == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Workgroup workgroup = schedule.getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "instructor");

        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.updatePreferenceOrder(sortedTeachingPreferenceIds);

        if (teachingAssignments == null || teachingAssignments.size() != sortedTeachingPreferenceIds.size()) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        return sortedTeachingPreferenceIds;
    }
}
