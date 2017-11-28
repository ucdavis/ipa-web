package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class WorkgroupViewController {
	@Inject WorkgroupViewFactory workgroupViewFactory;
	@Inject Authorizer authorizer;

	@RequestMapping(value = "/api/workgroupView/{workgroupId}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public WorkgroupView getWorkgroupViewByCode(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return workgroupViewFactory.createWorkgroupView(workgroupId);
	}
}
