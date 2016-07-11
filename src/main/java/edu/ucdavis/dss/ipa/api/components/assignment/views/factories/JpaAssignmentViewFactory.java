package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.entities.*;
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

	@Override
	public AssignmentView createAssignmentView(long workgroupId, long year) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Schedule schedule = scheduleService.findByWorkgroupAndYear(workgroup, year);

		List<Course> courses = schedule.getCourses();
		List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);
		List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<ScheduleInstructorNote> scheduleInstructorNotes = scheduleInstructorNoteService.findByScheduleId(schedule.getId());
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);

		return new AssignmentView(courses, sectionGroups, teachingAssignments, instructors, scheduleInstructorNotes, scheduleTermStates);
	}
}
