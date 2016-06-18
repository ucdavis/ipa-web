package edu.ucdavis.dss.ipa.api.components.teachingCall;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallByCourseView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallByInstructorView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallCourseOfferingView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallSummaryView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import org.springframework.web.servlet.View;

@RestController
public class TeachingCallViewController {
	@Inject CurrentUser currentUser;
	@Inject InstructorService instructorService;
	@Inject AuthenticationService authenticationService;
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleService scheduleService;
	@Inject TeachingCallService teachingCallService;
	@Inject TeachingPreferenceService teachingPreferenceService;
	@Inject TeachingCallViewFactory teachingCallViewFactory;

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')")
	@RequestMapping(value = "/api/teachingCalls/{teachingCallId}", method = RequestMethod.GET)
	@ResponseBody
	public TeachingCallSummaryView getTeachingCallById(@PathVariable long teachingCallId, HttpServletResponse response)
	{
		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		if(teachingCall == null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		UserLogger.log(currentUser, "Loaded the " + teachingCall.getSchedule().getWorkgroup().getName() + " Teaching Call "
				+ " for the year " + teachingCall.getSchedule().getYear() + " teachingCallId: " + teachingCallId);
		response.setStatus(HttpStatus.OK.value());
		return this.teachingCallViewFactory.createTeachingCallSummaryView(teachingCall);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCallByCourse", method = RequestMethod.GET)
	@ResponseBody
	public List<TeachingCallByCourseView> getTeachingCallByCourse (
			@PathVariable Long scheduleId, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findById(scheduleId);

		return teachingCallViewFactory.createTeachingCallByCourseView(schedule);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')"
			+ "or hasPermission(#scheduleId, 'schedule', 'senateInstructor') or hasPermission(#scheduleId, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCallCourseOfferings", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<TeachingCallCourseOfferingView> getTeachingCallCourseOfferingsBySchedule(
			@PathVariable long scheduleId,
			HttpServletResponse httpResponse)
	{
		Schedule schedule = scheduleService.findById(scheduleId);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		httpResponse.setStatus(HttpStatus.OK.value());
		return this.teachingCallViewFactory.createTeachingCallCourseOfferingView(schedule);
	}

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')")
	@RequestMapping(value = "/api/teachingCalls/{teachingCallId}/courseOfferings", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<TeachingCallCourseOfferingView> getTeachingCallCourseOfferingsByTeachingCall(
			@PathVariable long teachingCallId,
			HttpServletResponse httpResponse)
	{
		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);

		if (teachingCall == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		httpResponse.setStatus(HttpStatus.OK.value());
		return this.teachingCallViewFactory.createTeachingCallCourseOfferingView(teachingCall.getSchedule());
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCallByInstructor", method = RequestMethod.GET)
	@ResponseBody
	public List<TeachingCallByInstructorView> getTeachingCallInstructors (
			@PathVariable Long scheduleId, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findById(scheduleId);

		return teachingCallViewFactory.createTeachingCallByInstructorView(schedule);
	}


	/**
	 * Exports a teaching preferences view with Schedule ID 'id' as an Excel .xls file
	 *
	 * @param scheduleId
	 * @return
	 */
	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingPreferences/excel")
	public View teachingPreferencesExcelExport(@PathVariable long scheduleId, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findById(scheduleId);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return teachingCallViewFactory.createTeachingPreferencesExcelView(teachingCallViewFactory.createTeachingCallByInstructorView(schedule));
	}
}
