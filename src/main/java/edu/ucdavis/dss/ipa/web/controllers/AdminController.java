package edu.ucdavis.dss.ipa.web.controllers;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.ucdavis.dss.ipa.config.annotation.WebController;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@WebController
public class AdminController {
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject AuthenticationService authenticationService;
	
	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/admin", method = RequestMethod.GET)
	public String admin()
	{
		return "admin";
	}

	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/impersonate/{loginId}", method = RequestMethod.GET)
	public String impersonateUser(@PathVariable String loginId, HttpServletResponse httpResponse) {
		User user = this.userService.getUserByLoginId(loginId);
		if (user != null) {
			authenticationService.impersonateUser(loginId);
			httpResponse.setStatus(HttpStatus.OK.value());
			return "redirect:/summary";
		} else {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
			return "notfound";
		}

	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/unImpersonate", method = RequestMethod.GET)
	public String unimpersonateUser() {
		authenticationService.unImpersonateUser();
		return "redirect:/summary";
	}

}
