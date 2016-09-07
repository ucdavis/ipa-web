package edu.ucdavis.dss.ipa.api.components.admin;

import edu.ucdavis.dss.ipa.api.components.admin.views.AdminView;
import edu.ucdavis.dss.ipa.api.components.admin.views.factories.AdminViewFactory;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class AdminViewController {
	@Inject AdminViewFactory adminViewFactory;

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

}
