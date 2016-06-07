package edu.ucdavis.dss.ipa.web.controllers.api;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.ucdavis.dss.ipa.config.annotation.WebController;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserService;

@WebController
public class RoleController {
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject RoleService roleService;
	
	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/api/roles", method = RequestMethod.GET)
	@ResponseBody
	public List<Role> getAllRoles() {
		return this.roleService.getAllRoles();
	}
}
