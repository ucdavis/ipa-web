package edu.ucdavis.dss.ipa.web.controllers.api;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;

@RestController
public class TeachingAssignmentController {

	@Inject TeachingAssignmentService teachingAssignmentService;

	@PreAuthorize("hasPermission(#teachingAssignmentId, 'teachingAssignment', 'academicCoordinator')")
	@RequestMapping(value = "/api/teachingAssignments/{teachingAssignmentId}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteInstructorAssignment(
			@PathVariable long teachingAssignmentId,
			HttpServletResponse httpResponse) {
		teachingAssignmentService.deleteTeachingAssignmentById(teachingAssignmentId);
	}

}
