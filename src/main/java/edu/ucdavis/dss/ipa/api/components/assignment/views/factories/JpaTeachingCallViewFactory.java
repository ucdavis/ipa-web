package edu.ucdavis.dss.ipa.api.components.assignment.views.factories;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallByCourseView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallByInstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallSectionGroupView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallInstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallScheduleView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallSummaryView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingPreferencesExcelView;

@Service
public class JpaTeachingCallViewFactory implements TeachingCallViewFactory {
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;
	@Inject UserRoleService userRoleService;
	@Inject InstructorService instructorService;
	@Inject ScheduleInstructorNoteService scheduleInstructorNoteService;
	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject TeachingAssignmentService teachingAssignmentService;

	@Override
	public TeachingCallScheduleView createTeachingCallScheduleView(Schedule schedule) {
		TeachingCallScheduleView teachingCallScheduleView = new TeachingCallScheduleView();

		teachingCallScheduleView.setImporting(schedule.isImporting());
		teachingCallScheduleView.setId(schedule.getId());
		teachingCallScheduleView.setWorkgroupId(schedule.getWorkgroup().getId());
		teachingCallScheduleView.setYear(schedule.getYear());
		teachingCallScheduleView.setTeachingCalls(schedule.getTeachingCalls());
		List<String> terms = scheduleService.getActiveTermCodesForSchedule(schedule);
		teachingCallScheduleView.setScheduleTermStates(scheduleTermStateService.getScheduleTermStatesBySchedule(schedule));
		teachingCallScheduleView.setTerms(terms);

		return teachingCallScheduleView;
	}

	@Override
	public List<TeachingCallSectionGroupView> createTeachingCallCourseOfferingView(Schedule schedule) {
		List<TeachingCallSectionGroupView> teachingCallSectionGroupViews = new ArrayList<TeachingCallSectionGroupView>();

		for (Course course : schedule.getCourses()) {
			for (SectionGroup sectionGroup : course.getSectionGroups()) {
				TeachingCallSectionGroupView teachingCallSectionGroupView = new TeachingCallSectionGroupView(sectionGroup);
				teachingCallSectionGroupViews.add(teachingCallSectionGroupView);
			}
		}

		return teachingCallSectionGroupViews;
	}

	@Override
	public List<TeachingCallByInstructorView> createTeachingCallByInstructorView(Schedule schedule) {
		List<TeachingCallByInstructorView> teachingCallByInstructorViews = new ArrayList<TeachingCallByInstructorView>();
		List<Instructor> workgroupInstructors = userRoleService.getInstructorsByWorkgroupId(schedule.getWorkgroup().getId());

		for (Instructor instructor: workgroupInstructors) {
			List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();
			TeachingCallReceipt teachingCallReceipt = null;

			for (TeachingCall teachingCall: schedule.getTeachingCalls()) {
				// Skip if this instructor was already processed in another teachingCall
				if (teachingCallResponses.size() > 0 || teachingCallReceipt != null) { continue; }

				teachingCallResponses = teachingCallResponseService.findByTeachingCallAndInstructorLoginId(teachingCall, instructor.getLoginId());
				teachingCallReceipt = teachingCallReceiptService.findByTeachingCallIdAndInstructorLoginId(teachingCall.getId(), instructor.getLoginId());
			}

			List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByScheduleIdAndInstructorId(schedule.getId(), instructor.getId());
			ScheduleInstructorNote scheduleInstructorNote = scheduleInstructorNoteService.findOneByInstructorIdAndScheduleId(instructor.getId(), schedule.getId());

			teachingCallByInstructorViews.add(
					new TeachingCallByInstructorView(
							instructor,
							teachingAssignments,
							teachingCallResponses,
							teachingCallReceipt,
							scheduleInstructorNote,
							schedule.getYear()
						)
				);
		}

		return teachingCallByInstructorViews;
	}
	
	@Override
	public List<TeachingCallByCourseView> createTeachingCallByCourseView(Schedule schedule) {
		List<TeachingCallByCourseView> teachingCallByCourseViews = new ArrayList<TeachingCallByCourseView>();

		for (Course course : schedule.getCourses()) {
			List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByCourseId(course.getId());
			teachingCallByCourseViews.add(new TeachingCallByCourseView(course, teachingAssignments));
		}

		return teachingCallByCourseViews;
	}

	@Override
	public List<TeachingCallInstructorView> createWorkgroupInstructors(Workgroup workgroup) {
		List<TeachingCallInstructorView> instructors = new ArrayList<TeachingCallInstructorView>();

		for (UserRole userRole: workgroup.getUserRoles()) {
			if (UserRole.isInstructor(userRole)) {
				Instructor instructor = instructorService.getOneByLoginId(userRole.getUser().getLoginId());
				TeachingCallInstructorView teachingCallInstructorView = new TeachingCallInstructorView(instructor);

				if (!instructors.contains(teachingCallInstructorView)) {
					instructors.add(new TeachingCallInstructorView(instructor));
				}
			}
		}

		return instructors;
	}

	@Override
	public TeachingCallSummaryView createTeachingCallSummaryView(TeachingCall teachingCall) {
		List<String> terms = scheduleService.getActiveTermCodesForSchedule(teachingCall.getSchedule());
		TeachingCallSummaryView teachingCallSummaryView = new TeachingCallSummaryView(teachingCall, terms);
		return teachingCallSummaryView;
	}

	@Override
	public View createTeachingPreferencesExcelView(List<TeachingCallByInstructorView> TeachingCallInstructors) {
		return new TeachingPreferencesExcelView(TeachingCallInstructors);
	}
}
