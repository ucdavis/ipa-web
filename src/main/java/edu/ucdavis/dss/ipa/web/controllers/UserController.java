package edu.ucdavis.dss.ipa.web.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.entities.GraduateStudent;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.web.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.web.views.UserViews;
import edu.ucdavis.dss.ipa.web.views.WorkgroupUserRolesView;
import edu.ucdavis.dss.ipa.web.views.WorkgroupViews;

@RestController
public class UserController {
	private static final Logger log = LogManager.getLogger();
	
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject ScheduleService scheduleService;
	@Inject WorkgroupService workgroupService;
	@Inject RoleService roleService;
	@Inject UserRoleService userRoleService;
	@Inject GraduateStudentService graduateStudentService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject CurrentUser currentUser;

	@JsonView(UserViews.Simple.class)
	@RequestMapping(value = "/api/current-user.json", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public User currentUser() {
		String loginId = authenticationService.getCurrentUser().getLoginid();
		
		return userService.getUserByLoginId(loginId);
	}

	@RequestMapping(value = "/api/workgroups/{workgroupId}/userRoles", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public WorkgroupUserRolesView getWorkgroupUsers(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);
		if (workgroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
		return new WorkgroupUserRolesView(workgroup);
	}

	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/api/users", method = RequestMethod.GET)
	@JsonView(UserViews.Detailed.class)
	@ResponseBody
	public List<User> allUsers() {
		return this.userService.getAllUsers();
	}

	@RequestMapping(value = "/api/users/{userId}/workgroups", method = RequestMethod.GET)
	@JsonView(WorkgroupViews.Summary.class)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Workgroup> getUserWorkgroups(@PathVariable Long userId, HttpServletResponse httpResponse) {
		User user = this.userService.getUserById(userId);
		List<Workgroup> workgroupList = null;
		
		if(user != null) {
			workgroupList = user.getWorkgroups();
			httpResponse.setStatus(HttpStatus.OK.value());
		} else {
			log.error("/api/users/{userId}/workgroups passed an invalid user ID.");
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		}

		return workgroupList;
	}

	@RequestMapping(value = "/api/users/{id}/roles", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Role> getUserRoles(@PathVariable Long id, HttpServletResponse httpResponse) {
		List<Role> roleList = this.userService.getUserById(id).getRoles();

		httpResponse.setStatus(HttpStatus.OK.value());
		return roleList;
	}

	@RequestMapping(value = "/api/users/{loginId}/workgroups/{workgroupId}/roles/{role}", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	public List<UserRole> addUserRoleToUser(@PathVariable String loginId, @PathVariable Long workgroupId, @PathVariable String role, HttpServletResponse httpResponse) {
		User user = this.userService.getUserByLoginId(loginId);
		Role newRole = roleService.findOneByName(role);

		// Has the user been added to IPA already?
		if (user == null) {
			user = userService.findOrCreateUserByLoginId(loginId);
		}

		//TODO: Ideally this scenario should be handled by a more nuanced hasPermission method
		// Only Admins can modify the admin role
		if ("admin".equals(role) && !user.isAdmin()) {
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return null;
		}

		// Is the role invalid?
		if (newRole == null) {
			log.error("Attempted to add userRole failed: role was invalid.");
			// Has the user been added to IPA already?
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		} 
		else {
			userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, role);
			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Added role '" + role + "' to user " + user.getName() + " (" + loginId + ")");
		}

		// query new roleList and return
		List<UserRole> roleList = userService.getUserByLoginId(loginId).getUserRoles();

		return roleList;
	}

	@RequestMapping(value = "/api/users/{loginId}/workgroups/{workgroupId}/roles/{role}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	public List<UserRole> removeUserRoleFromUser(@PathVariable String loginId, @PathVariable Long workgroupId, @PathVariable String role, HttpServletResponse httpResponse) {
		Role newRole = roleService.findOneByName(role);
		User user = this.userService.getUserByLoginId(loginId);

		//TODO: Ideally this scenario should be handled by a more nuanced hasPermission method
		// Only Admins can modify the admin role
		if ("admin".equals(role) && !user.isAdmin()) {
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return null;
		}

		// Is the role invalid?
		if (newRole == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		} else {
			userRoleService.deleteByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, role);

			// Query new roleList and return
			user = this.userService.getUserByLoginId(loginId);
			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Removed role '" + role + "' from user " + user.getName() + " (" + loginId + ")");
		}

		return user.getUserRoles();
	}

	@RequestMapping(value = "/api/users/{loginId}/workgroups/{workgroupId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	public void removeUserFromDepartment(@PathVariable String loginId, @PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		if(userRoleService.deleteByLoginIdAndWorkgroupId(loginId, workgroupId)) {
			User user = this.userService.getUserByLoginId(loginId);
			UserLogger.log(currentUser, "Removed user " + user.getName() + " (" + loginId + ") from workgroup with ID " + workgroupId);
			httpResponse.setStatus(HttpStatus.OK.value());
		} else {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		}
	}

	/**
	 * Searches locally or in the Data Warehouse for the user.
	 * 
	 * @param query
	 * @return
	 */
	@RequestMapping(value = "/api/users/{query}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<User> searchUsers(@PathVariable String query, @RequestParam(value = "localOnly", required = false) boolean localOnly) {
		if(localOnly) {
			return this.userService.searchAllUsersByFirstLastAndLoginId(query);
		} else {
			List<User> users = new ArrayList<User>();
			List<DwPerson> dwPeople = new ArrayList<DwPerson>();
	
			try {
				dwPeople = dwRepository.searchPeople(query);
	
				for (DwPerson dwPerson : dwPeople) {
					User user = new User();
					user.setLoginId(dwPerson.getLoginId());
					user.setEmail(dwPerson.getEmail());
					user.setFirstName(dwPerson.getFirst());
					user.setLastName(dwPerson.getLast());
					users.add(user);
				}
			} catch (Exception e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
	
			return users;
		}
	}

	// Returns a list of graduate students from the specified workgroup who are
	// If requiredActive is true it will only return graduate students who have userRoles 
	// (capable of TAing, as opposed to grad students no longer at the workgroup)
	// This action will NOT return graduate students who have TA'd for courses for the specified workgroup
	@PreAuthorize("hasPermission(#id, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/workgroups/{id}/teachingAssistants", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public List<GraduateStudent> getActiveTeachingAssistantsByWorkgroupId(@PathVariable long id, HttpServletResponse httpResponse, @RequestParam(value = "requireActive", required = true) Boolean requireActive) {
		List<GraduateStudent> teachingAssistants = new ArrayList<GraduateStudent>();

		if(requireActive) {
			teachingAssistants = graduateStudentService.getAllTeachingAssistantGraduateStudentsByWorkgroupId(id);
		}

		// Simply get all graduate students, past and present, associated to the workgroup
		else {
			Workgroup workgroup = workgroupService.findOneById(id);
			teachingAssistants = workgroup.getGraduateStudents();
		}
		return teachingAssistants;
	}

	@PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/workgroups/{workgroupId}/users", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public List<User> getUsersByWorkgroupId(@PathVariable long workgroupId, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);

		List<User> users = new ArrayList<User>();

		for (UserRole userRole : workgroup.getUserRoles()) {
			if(!users.contains(userRole.getUser()) ) {
				users.add(userRole.getUser());
			}
		}

		return users;
	}

	// SECUREME: IP Whitelising
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value ="/active-users", method = RequestMethod.GET)
	@ResponseBody
	public String getAllActiveUsers(Model model, HttpServletResponse httpResponse) {
		httpResponse.setContentType("text/plain");
		httpResponse.setCharacterEncoding("UTF-8");

		List<User> activeUsers = userService.getAllUsers();
		StringJoiner sjActiveUsers = new StringJoiner(System.getProperty("line.separator"));

		for (User user: activeUsers) {
			StringJoiner sjUser = new StringJoiner(" ");
			sjUser.add(user.getEmail());
			sjUser.add(user.getFirstName());
			sjUser.add(user.getLastName());
			sjActiveUsers.add(sjUser.toString());
		}

		return sjActiveUsers.toString();
	}

}
