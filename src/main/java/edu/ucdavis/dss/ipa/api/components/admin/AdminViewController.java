package edu.ucdavis.dss.ipa.api.components.admin;

import edu.ucdavis.dss.ipa.api.components.admin.views.AdminView;
import edu.ucdavis.dss.ipa.api.components.admin.views.factories.AdminViewFactory;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class AdminViewController {
	@Inject AdminViewFactory adminViewFactory;
	@Inject WorkgroupService workgroupService;

	/**
	 * Delivers the JSON payload for the Admin View, used on page load.
	 *
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/adminView", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public AdminView showAdminView(HttpServletResponse httpResponse) {
		Authorizer.isAdmin();

		return adminViewFactory.createAdminView();
	}

	@RequestMapping(value = "/api/adminView/workgroups", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Workgroup addWorkgroup(@RequestBody Workgroup workgroup, HttpServletResponse httpResponse) {
		Authorizer.isAdmin();

		return workgroupService.save(workgroup);
	}

	@RequestMapping(value = "/api/adminView/workgroups/{workgroupId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Workgroup updateWorkgroup(@PathVariable Long workgroupId,
								   @RequestBody Workgroup workgroup, HttpServletResponse httpResponse) {
		Authorizer.isAdmin();

		Workgroup editedWorkgroup = workgroupService.findOneById(workgroupId);
		editedWorkgroup.setName(workgroup.getName());
		return workgroupService.save(editedWorkgroup);
	}

	@RequestMapping(value = "/api/adminView/workgroups/{workgroupId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void removeWorkgroup(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
		Authorizer.isAdmin();

		workgroupService.delete(workgroupId);
	}

}
