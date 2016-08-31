package edu.ucdavis.dss.ipa.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.dw.dto.DwDepartment;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.TeachingCallResponseViews;
import edu.ucdavis.dss.ipa.api.views.WorkgroupViews;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.utilities.UserLogger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
public class WorkgroupController {
	@Inject WorkgroupService workgroupService;
	@Inject InstructorService instructorService;
	@Inject TagService tagService;
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject UserRoleService userRoleService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleOpsService scheduleOpsService;
	@Inject CourseService courseService;
	@Inject WorkgroupOpsService workgroupOpsService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject TermService termService;
	@Inject CurrentUser currentUser;
	@Inject TeachingCallResponseService teachingCallResponseService;

	@PreAuthorize("hasPermission(#Id, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroups/{Id}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(WorkgroupViews.Summary.class)
	public Workgroup workgroupById(@PathVariable Long Id) {
		return this.workgroupService.findOneById(Id);
	}

	@RequestMapping(value = "/workgroups", method = RequestMethod.GET, produces="text/html")
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public String index() {
		return "workgroup";
	}

	@PreAuthorize("hasPermission(#Id, 'workgroup', 'academicCoordinator') or hasPermission(#Id, 'workgroup', 'senateInstructor') or hasPermission(#Id, 'workgroup', 'federationInstructor')")
	@RequestMapping(value ="/api/workgroups/{Id}/teachingCalls", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public List<TeachingCallResponse> getInstructorTeachingCallResponsesByWorkgroupId(@PathVariable long Id, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(Id);

		String loginId = authenticationService.getCurrentUser().getLoginid();
		Instructor instructor = instructorService.getOneByLoginId(loginId);

		return this.teachingCallResponseService.getWorkgroupTeachingCallResponsesByInstructorId(workgroup, instructor);
	}

	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/api/departments", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(WorkgroupViews.Summary.class)
	public List<DwDepartment> allDepartments(HttpServletResponse httpResponse) {
		List<DwDepartment> workgroups = new ArrayList<DwDepartment>();

		try {
			workgroups = dwRepository.getAllSisDepartments();
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}

		return workgroups;
	}

	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/api/workgroups", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(WorkgroupViews.Summary.class)
	public Workgroup createWorkgroup(@RequestBody Workgroup workgroup, HttpServletResponse httpResponse) {
		workgroup = this.workgroupOpsService.provisionNewWorkgroup(workgroup);

		httpResponse.setStatus(HttpStatus.OK.value());
		UserLogger.log(currentUser, "Created new workgroup '" + workgroup.getName() + " (" + workgroup.getCode() + ")");

		return workgroup;
	}

	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroups/ipa", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(WorkgroupViews.Summary.class)
	public List<Workgroup> allIpaWorkgroups(HttpServletResponse httpResponse) {
		List<Workgroup> workgroups = workgroupService.findAll();
		return workgroups;
	}

	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/api/workgroups/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteWorkgroup (@PathVariable long id, HttpServletResponse httpResponse) {
		if(workgroupOpsService.deleteWorkgroup(id)) {
			httpResponse.setStatus(HttpStatus.OK.value());
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}
	}

	@PreAuthorize("hasPermission('*', 'admin')")
	@RequestMapping(value = "/api/workgroups/{id}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(WorkgroupViews.Summary.class)
	public Workgroup updateWorkgroupName(@PathVariable long id, @RequestBody Workgroup workgroup, HttpServletResponse httpResponse) {
		Workgroup ipaWorkgroup = this.workgroupService.findOneById(id);

		if (ipaWorkgroup != null) {
			httpResponse.setStatus(HttpStatus.OK.value());
			ipaWorkgroup.setName(workgroup.getName());
			return workgroupService.save(ipaWorkgroup);
		}

		httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		return workgroup;
	}

	@PreAuthorize("isAuthenticated()")
	// SECUREME - If currentUser cannot switch to this workgroup, it should deny them.
	@RequestMapping(value = "/setActiveWorkgroup/{workgroupId}", method = RequestMethod.GET)
	public String setActiveWorkgroup(@PathVariable long workgroupId) {
		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);

		if (currentUser.getWorkgroupIds().contains(workgroupId)) {
			authenticationService.setActiveWorkgroupForCurrentUser(workgroup);
		}

		return "redirect:/summary";
	}

}
