package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentExcelView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.InstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.InstructorExcelView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject UserService userService;
	@Inject SupportAssignmentService supportAssignmentService;
	@Inject TermService termService;

	@Override
	public AssignmentView createAssignmentView(long workgroupId, long year, long userId, long instructorId) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		long scheduleId = schedule.getId();

		List<Instructor> instructorMasterList = instructorService.findByScheduleId(scheduleId);
		List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		List<SectionGroup> sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYear(workgroupId, year);

		List<SupportAssignment> supportAssignments = supportAssignmentService.findBySectionGroups(sectionGroups);
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<ScheduleInstructorNote> scheduleInstructorNotes = scheduleInstructorNoteService.findByScheduleId(schedule.getId());
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		List<TeachingCallReceipt> teachingCallReceipts = schedule.getTeachingCallReceipts();
		List<TeachingCallResponse> teachingCallResponses = schedule.getTeachingCallResponses();
		List<Long> senateInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "senateInstructor");
		List<Long> federationInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "federationInstructor");
		List<Long> lecturerInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "lecturer");

		return new AssignmentView(courses, sectionGroups, schedule.getTeachingAssignments(), instructors, instructorMasterList,
				scheduleInstructorNotes, scheduleTermStates, teachingCallReceipts,
				teachingCallResponses, userId, instructorId, scheduleId,
				senateInstructorIds, federationInstructorIds, lecturerInstructorIds, workgroup.getTags(), supportAssignments);
	}

	@Override
	public InstructorView createInstructorView(long workgroupId, long year) {
		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		Workgroup workgroup = schedule.getWorkgroup();

		if(workgroup == null) { return null; }

		List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroup.getId(), schedule.getYear());
		List<Instructor> instructors = instructorService.findByScheduleId(schedule.getId());
		List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleId(schedule.getId());
		List<Course> courses = schedule.getCourses();
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);

		List<Term> terms = new ArrayList<>();

		for (ScheduleTermState scheduleTermState : scheduleTermStates) {
			terms.add(termService.getOneByTermCode(scheduleTermState.getTermCode()));
		}


		return new InstructorView(courses, sectionGroups, instructors, teachingAssignments, terms, schedule.getId());
	}

	@Override
	public View createAssignmentExcelView(long workgroupId, long year, String pivot) {
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);

		if ("course".equals(pivot)) {
			List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
			return new AssignmentExcelView(schedule, instructors, scheduleTermStates);
		} else {
			InstructorView instructorView = createInstructorView(workgroupId, year);

			return new InstructorExcelView(instructorView);
		}
	}
}
