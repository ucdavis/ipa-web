package edu.ucdavis.dss.ipa.api.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.UserService;

@RestController
public class TermController {
	@Inject AuthenticationService authenticationService;
	@Inject ScheduleService scheduleService;
	@Inject UserService userService;
	@Inject TermService termService;
	
	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/termView", method = RequestMethod.GET)
	public String getTermView() {
		return "termView";
	}
	
	@RequestMapping(value = "/api/schedules/{scheduleId}/terms/{termCode}/emailInstructorTermView", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public void sendInstructorTermViewEmail(@PathVariable Long scheduleId, @PathVariable String termCode, HttpServletResponse httpResponse) {
		Schedule schedule = scheduleService.findById(scheduleId);
		String messageBody = SettingsConfiguration.getURL() + "/schedules/" + scheduleId + "/terms/" + termCode;

		String subject = "IPA: " + schedule.getWorkgroup().getName() + " Course Plan is available for review";
		
		List<User> users = scheduleService.getUserInstructorsByScheduleIdAndTermCode(scheduleId, termCode);
		
		for(User user : users) {
			userService.contact(user, messageBody, subject);
		}

		httpResponse.setStatus(HttpStatus.OK.value());
	}

	@RequestMapping(value = "/api/terms/{year}", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Term> getTermsByYear(@PathVariable String year) {
		return termService.findByYear(year);
	}

	@RequestMapping(value = { "/api/terms/{termCode}" }, method = { RequestMethod.PUT })
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Term updateTerm(@RequestBody Term term, @PathVariable("termCode") String termCode, HttpServletResponse httpResponse_p) {
		Term updatedTerm = this.termService.updateOrCreate(term);
		
		if (updatedTerm != null) {
			httpResponse_p.setStatus(HttpStatus.OK.value());
		}
		else {
			httpResponse_p.setStatus(HttpStatus.FORBIDDEN.value());
		}

		return termService.getOneByTermCode(termCode);
	}

}