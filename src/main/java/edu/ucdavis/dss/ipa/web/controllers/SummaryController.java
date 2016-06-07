package edu.ucdavis.dss.ipa.web.controllers;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.utilities.UserLogger;
import edu.ucdavis.dss.ipa.web.helpers.CurrentUser;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the Summary screen.
 */
@RestController
public class SummaryController {
	@Inject WorkgroupService workgroupService;
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;
	@Inject CurrentUser currentUser;

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/summary", method = RequestMethod.GET)
	public String dashboard(Model model) {
		UserLogger.log(currentUser, "Loaded dashboard.");
		return "summary";
	}
}