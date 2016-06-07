package edu.ucdavis.dss.ipa.web.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for the public pages.
 * 
 * @author Christopher Thielen
 *
 */
@RestController
public class WelcomeController {
	
	@PreAuthorize("permitAll")
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String welcome() {
		return "welcome";
	}
	
	@PreAuthorize("permitAll")
	@RequestMapping(value = "/learn", method = RequestMethod.GET)
	public String learn() {
		return "learn";
	}

	@PreAuthorize("permitAll")
	@RequestMapping(value = "/announcements", method = RequestMethod.GET)
	public String announcements() {
		return "announcements";
	}
}
