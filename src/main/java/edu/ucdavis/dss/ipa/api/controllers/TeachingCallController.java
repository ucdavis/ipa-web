package edu.ucdavis.dss.ipa.api.controllers;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.UserService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeachingCallController {
	private static final Logger log = LogManager.getLogger();

	@Inject TeachingCallService teachingCallService;
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;

	/*
	 * This is a temporary redirect of the old teachingCall link for
	 * Instructors who might still have old links from active teachingCalls
	 */
	@RequestMapping(value = "/teachingCalls/{teachingCallId}", method = RequestMethod.GET)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public String tempTeachingCallRedirect(@PathVariable Long teachingCallId, HttpServletResponse httpResponse) {
		return "redirect:/teachingCalls/#/" + teachingCallId;
	}

	@RequestMapping(value = "/teachingCalls", method = RequestMethod.GET)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public String teachingCall(HttpServletResponse httpResponse) {
		String loginId = authenticationService.getCurrentUser().getLoginid();
		User user = userService.getUserByLoginId(loginId);
		Long userId = null;
		
		if(user != null) userId = user.getId();
		
		if (userId == null) {
			log.error("Cannot return teaching call: user ID unknown.");
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return "";
		}
		
		Instructor instructor = instructorService.getInstructorByLoginId(loginId);
		
		if (instructor == null) {
			log.warn("Cannot get teaching call: no such instructor for user " + user);
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return "";
		}

		return "teachingCall";
	}
}
