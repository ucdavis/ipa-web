package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallByCourseView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallByInstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallSectionGroupView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallInstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallScheduleView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingCallSummaryView;
import edu.ucdavis.dss.ipa.api.components.assignment.deprecated.TeachingPreferencesExcelView;

@Service
public class JpaAssignmentViewFactory implements AssignmentViewFactory {
	@Inject WorkgroupService workgroupService;
	@Inject InstructorService instructorService;
	@Inject ScheduleInstructorNoteService scheduleInstructorNoteService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject SectionGroupService sectionGroupService;
	@Inject CourseService courseService;
	@Inject UserRoleService userRoleService;
	@Inject TeachingCallService teachingCallService;
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject UserService userService;

	@Override
	public AssignmentView createAssignmentView(long workgroupId, long year, long userId, long instructorId) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
		long scheduleId = schedule.getId();
		List<Course> courses = schedule.getCourses();
		List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<ScheduleInstructorNote> scheduleInstructorNotes = scheduleInstructorNoteService.findByScheduleId(schedule.getId());
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		List<TeachingCallReceipt> teachingCallReceipts = teachingCallReceiptService.findByScheduleId(schedule.getId());
		List<TeachingCallResponse> teachingCallResponses = teachingCallResponseService.findByScheduleId(schedule.getId());
		TeachingCall activeTeachingCall = teachingCallService.findOneByUserIdAndScheduleId(userId, schedule.getId());
		List<Long> senateInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "senateInstructor");
		List<Long> federationInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "federationInstructor");

		return new AssignmentView(courses, sectionGroups, schedule.getTeachingAssignments(), instructors,
				scheduleInstructorNotes, scheduleTermStates, schedule.getTeachingCalls(), teachingCallReceipts,
				teachingCallResponses, activeTeachingCall, userId, instructorId, scheduleId,
				senateInstructorIds, federationInstructorIds, workgroup.getTags());
	}
}
