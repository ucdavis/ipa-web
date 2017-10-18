package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class WorkgroupViewController {

	@Inject WorkgroupViewFactory workgroupViewFactory;
	@Inject WorkgroupService workgroupService;

	@RequestMapping(value = "/api/workgroupView/{workgroupId}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public WorkgroupView getWorkgroupViewByCode(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return workgroupViewFactory.createWorkgroupView(workgroupId);
	}
}
