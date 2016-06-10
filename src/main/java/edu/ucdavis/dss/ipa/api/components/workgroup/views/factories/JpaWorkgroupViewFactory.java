package edu.ucdavis.dss.ipa.api.components.workgroup.views.factories;

import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualView;
import edu.ucdavis.dss.ipa.api.components.annual.views.ScheduleExcelView;
import edu.ucdavis.dss.ipa.api.components.annual.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupUserView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaWorkgroupViewFactory implements WorkgroupViewFactory {

	@Inject WorkgroupService workgroupService;
	@Inject UserRoleService userRoleService;
	@Inject RoleService roleService;

	@Override
	public WorkgroupView createWorkgroupView(String workgroupCode) {
		Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
		List<UserRole> userRoles = userRoleService.findByWorkgroup(workgroup);
		List<Role> roles = roleService.getAllRoles();

		List<WorkgroupUserView> users = new ArrayList<WorkgroupUserView>();

		for (UserRole userRole : workgroup.getUserRoles()) {
			if(!users.contains(userRole.getUser()) ) {
				users.add(new WorkgroupUserView(userRole.getUser()));
			}
		}

		return new WorkgroupView(workgroup, userRoles, roles, users);
	}

}
