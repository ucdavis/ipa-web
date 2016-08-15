package edu.ucdavis.dss.ipa.api.components.scheduling;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.factories.SchedulingViewFactory;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class SchedulingViewController {

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
	public SchedulingView showCourseView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termCode,
										 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
										 HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		return schedulingViewFactory.createSchedulingView(workgroupId, year, termCode, showDoNotPrint);
	}

}
