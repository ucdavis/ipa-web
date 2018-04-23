package edu.ucdavis.dss.ipa.api.components.assignment;

import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.api.components.assignment.views.factories.AssignmentViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;

@RestController
@CrossOrigin
public class AssignmentViewSectionGroupController {
	@Inject CurrentUser currentUser;
	@Inject InstructorService instructorService;
	@Inject AuthenticationService authenticationService;
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleService scheduleService;
	@Inject AssignmentViewFactory teachingCallViewFactory;
	@Inject SectionGroupService sectionGroupService;
	@Inject Authorizer authorizer;

	@RequestMapping(value = "/api/assignmentView/{workgroupId}/{year}/sectionGroups", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<SectionGroup> getSectionGroupsByWorkgroupIdAndYear(
			@PathVariable long workgroupId,
			@PathVariable long year) {

		authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		return this.sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);
	}

}
