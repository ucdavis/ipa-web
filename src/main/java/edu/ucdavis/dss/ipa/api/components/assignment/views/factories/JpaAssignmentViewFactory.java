package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentExcelView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaAssignmentViewFactory implements AssignmentViewFactory {
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleInstructorNoteService scheduleInstructorNoteService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject SectionGroupService sectionGroupService;
	@Inject CourseService courseService;
	@Inject UserRoleService userRoleService;
	@Inject SupportAssignmentService supportAssignmentService;
	@Inject StudentSupportPreferenceService studentSupportPreferenceService;
	@Inject InstructorTypeService instructorTypeService;

	@Override
	public AssignmentView createAssignmentView(long workgroupId, long year, long userId, long instructorId) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		long scheduleId = schedule.getId();

		List<Instructor> instructorMasterList = userRoleService.findActiveInstructorsByScheduleId(scheduleId);
		List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		List<SectionGroup> sectionGroups = sectionGroupService.findByCourses(courses);
		List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

		List<SupportAssignment> supportAssignments = supportAssignmentService.findBySectionGroups(sectionGroups);
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<ScheduleInstructorNote> scheduleInstructorNotes = scheduleInstructorNoteService.findByScheduleId(schedule.getId());
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		List<TeachingCallReceipt> teachingCallReceipts = schedule.getTeachingCallReceipts();
		List<TeachingCallResponse> teachingCallResponses = schedule.getTeachingCallResponses();
		List<Long> instructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "instructor");

		List<SupportStaff> supportStaffList = userRoleService.findActiveSupportStaffByWorkgroupId(workgroupId);
		List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleId(scheduleId);

		return new AssignmentView(
				courses, sectionGroups, schedule.getTeachingAssignments(), instructors, instructorMasterList,
				scheduleInstructorNotes, scheduleTermStates, teachingCallReceipts,
				teachingCallResponses, userId, instructorId, scheduleId,
				instructorIds, workgroup.getTags(), supportAssignments, supportStaffList, studentSupportPreferences,
				instructorTypes
		);
	}

	@Override
	public View createAssignmentExcelView(long workgroupId, long year) {
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);

		return new AssignmentExcelView(schedule, instructors, scheduleTermStates);
	}
}
