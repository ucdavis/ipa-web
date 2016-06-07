package edu.ucdavis.dss.ipa.web.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.UserService;

@RestController
public class TeachingCallApiController {
	@Inject TeachingCallService teachingCallService;
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;

	@ResponseBody
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCalls", method = RequestMethod.POST)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public TeachingCall createTeachingCall(@PathVariable long scheduleId,
			@RequestBody TeachingCall teachingCallDTO,
			Model model, HttpServletResponse response) {
		Schedule schedule = scheduleService.findById(scheduleId);
		if(schedule == null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		TeachingCall teachingCall = teachingCallService.create(scheduleId, teachingCallDTO);
		if (teachingCall != null) {
			response.setStatus(HttpStatus.OK.value());
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return teachingCall;
	}

	@ResponseBody
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingCalls", method = RequestMethod.GET)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<TeachingCall> getTeachingCallsByScheduleId(@PathVariable long scheduleId, HttpServletResponse response) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if(schedule == null) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		List<TeachingCall> teachingCalls = schedule.getTeachingCalls();
		if (teachingCalls != null) {
			response.setStatus(HttpStatus.OK.value());
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		return teachingCalls;
	}
}
