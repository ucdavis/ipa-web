package edu.ucdavis.dss.ipa.api.components.scheduling;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingViewSectionGroup;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.factories.SchedulingViewFactory;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class SchedulingViewController {

	@Inject SectionGroupService sectionGroupService;
	@Inject SchedulingViewFactory schedulingViewFactory;

	/**
	 * Delivers the JSON payload for the Scheduling View (nee Activities View), used on page load.
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/schedulingView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public SchedulingView showSchedulingView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termCode,
										 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
										 HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		return schedulingViewFactory.createSchedulingView(workgroupId, year, termCode, showDoNotPrint);
	}

	/**
	 * Delivers sectionGroup details children including sections and their child activities
	 *
	 * @param sectionGroupId
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value = "/api/schedulingView/sectionGroups/{sectionGroupId}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public SchedulingViewSectionGroup getSectionGroupDetails(@PathVariable long sectionGroupId,
															 HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
		Authorizer.hasWorkgroupRole(sectionGroup.getCourse().getSchedule().getWorkgroup().getId(), "academicPlanner");

		return schedulingViewFactory.createSchedulingViewSectionGroup(sectionGroup);
	}

}
