package edu.ucdavis.dss.ipa.api.controllers.api;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.views.InstructorViews;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class InstructorController {
	@Inject InstructorService instructorService;
	@Inject AuthenticationService authenticationService;
	@Inject WorkgroupService workgroupService;
	@Inject UserRoleService userRoleService;

	@RequestMapping(value = "/api/workgroups/{id}/instructors", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(InstructorViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Instructor> getWorkgroupInstructors (@PathVariable Long id, HttpServletResponse httpResponse) {
		return this.userRoleService.getInstructorsByWorkgroupId(id);
	}

	@RequestMapping(value = "/api/instructors", method = RequestMethod.POST)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Instructor addInstructor(@RequestBody Instructor instructor, HttpServletResponse httpResponse) {
		Instructor newInstructor = this.instructorService.save(instructor);
		
		httpResponse.setStatus(HttpStatus.OK.value());
		
		return newInstructor;
	}

}
