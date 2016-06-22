package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewController {

	@Inject WorkgroupViewFactory workgroupViewFactory;
	@Inject WorkgroupService workgroupService;

	@PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/workgroupView/{workgroupId}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public WorkgroupView getWorkgroupViewByCode(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		return workgroupViewFactory.createWorkgroupView(workgroupId);
	}
}
