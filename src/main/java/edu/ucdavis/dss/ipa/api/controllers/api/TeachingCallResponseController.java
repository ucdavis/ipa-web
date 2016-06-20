package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.TeachingCallResponseService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.TeachingCallResponseViews;

@RestController
public class TeachingCallResponseController {
	private static final Logger log = LogManager.getLogger();

	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;
	@Inject TeachingCallService teachingCallService;
	@Inject CurrentUser currentUser;

	@RequestMapping(value = "/api/teachingCallResponses/{Id}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public TeachingCallResponse teachingCallResponse(@PathVariable Long Id) {
		return this.teachingCallResponseService.getOneById(Id);
	}

	@RequestMapping(value = "/api/teachingCallResponses", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public TeachingCallResponse createTeachingPreference(@RequestBody TeachingCallResponse teachingCallResponse) {
		UserLogger.log(currentUser, "Created a teaching call response for the termCode '" + teachingCallResponse.getTermCode());
		return this.teachingCallResponseService.save(teachingCallResponse);
	}

	@RequestMapping(value = "/api/teachingCallResponses/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public void deleteTeachingPreference(@PathVariable Long id, HttpServletResponse httpResponse) {
		TeachingCallResponse teachingCallResponse = this.teachingCallResponseService.getOneById(id);

		UserLogger.log(currentUser, "Deleted teaching call response for the termCode '" + teachingCallResponse.getTermCode());
		
		this.teachingCallResponseService.delete(id);
		
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'academicCoordinator')")
	@RequestMapping(value = "/api/teachingCalls/{teachingCallId}/teachingCallResponses", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public TeachingCallResponse updateTeachingCallResponse(
			@PathVariable Long teachingCallId,
			@RequestParam(value = "termCode", required = true) String termCode,
			@RequestParam(value = "instructorId", required = true) long instructorId,
			@RequestBody TeachingCallResponse teachingCallResponse,
			HttpServletResponse httpResponse) {
		TeachingCallResponse toBeSaved = this.teachingCallResponseService.findOrCreateOneByTeachingCallIdAndInstructorIdAndTermCode(
				teachingCallId, instructorId, termCode);

		if (toBeSaved == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		if (teachingCallResponse.getAvailabilityBlob() != null) {
			toBeSaved.setAvailabilityBlob(teachingCallResponse.getAvailabilityBlob());
			UserLogger.log(currentUser, "Changed availability for the termCode '" + teachingCallResponse.getTermCode());
		}

		return this.teachingCallResponseService.save(toBeSaved);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCallResponses", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public List<TeachingCallResponse> getTeachingCallResponsesForSchedule(
			@PathVariable long scheduleId,
			@RequestParam(value = "termCode", required = true) String termCode,
			HttpServletResponse httpResponse)
	{
		return teachingCallResponseService.findByScheduleIdAndTermCode(scheduleId, termCode);
	}

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')")
	@RequestMapping(value = "/api/teachingCalls/{teachingCallId}/teachingCallResponses", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public List<TeachingCallResponse> getTeachingCallResponses(
			@PathVariable long teachingCallId,
			HttpServletResponse httpResponse)
	{
		String loginId = authenticationService.getCurrentUser().getLoginid();
		Instructor instructor = instructorService.getOneByLoginId(loginId);
		if (instructor == null) {
			log.warn("Cannot get teaching call status: no such instructor.");
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		}

		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		if (teachingCall == null) {
			log.warn("Cannot get teaching call status: no such teaching call.");
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		}

		return teachingCallResponseService.findByTeachingCallAndInstructorLoginId(teachingCall, loginId);
	}
}
