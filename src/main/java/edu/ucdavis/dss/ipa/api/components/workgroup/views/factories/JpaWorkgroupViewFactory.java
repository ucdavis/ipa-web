package edu.ucdavis.dss.ipa.api.components.workgroup.views.factories;

import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualView;
import edu.ucdavis.dss.ipa.api.components.annual.views.ScheduleExcelView;
import edu.ucdavis.dss.ipa.api.components.annual.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.hibernate.jdbc.Work;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaWorkgroupViewFactory implements WorkgroupViewFactory {

	@Inject WorkgroupService workgroupService;
	@Inject UserRoleService userRoleService;

	@Override
	public WorkgroupView createWorkgroupView(String workgroupCode) {
		Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
		List<UserRole> userRoles = userRoleService.findByWorkgroup(workgroup);

		return new WorkgroupView(workgroup, userRoles);
	}

}
