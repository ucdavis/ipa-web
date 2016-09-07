package edu.ucdavis.dss.ipa.api.components.admin.views.factories;

import edu.ucdavis.dss.ipa.api.components.admin.views.AdminView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaAdminViewFactory implements AdminViewFactory {
	@Inject WorkgroupService workgroupService;

	@Override
	public AdminView createAdminView() {
		List<Workgroup> workgroups = workgroupService.findAll();

		return new AdminView(workgroups);
	}

}
