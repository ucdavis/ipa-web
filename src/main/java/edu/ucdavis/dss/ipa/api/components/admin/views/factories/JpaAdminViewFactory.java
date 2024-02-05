package edu.ucdavis.dss.ipa.api.components.admin.views.factories;

import edu.ucdavis.dss.ipa.api.components.admin.views.AdminView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaAdminViewFactory implements AdminViewFactory {
	@Inject WorkgroupService workgroupService;

	@Override
	public AdminView createAdminView() {
		List<Workgroup> workgroups = workgroupService.findAll();
		List<String> lastActiveDates = new ArrayList<>();

		for (Workgroup workgroup : workgroups) {
			String date = workgroupService.getLastActive(workgroup);
			String serializedActiveDate = workgroup.getId() + "," + date;
			lastActiveDates.add(serializedActiveDate);
		}

		return new AdminView(workgroups, lastActiveDates);
	}

}
