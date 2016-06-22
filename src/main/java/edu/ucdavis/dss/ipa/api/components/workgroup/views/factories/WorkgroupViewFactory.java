package edu.ucdavis.dss.ipa.api.components.workgroup.views.factories;

import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.entities.Schedule;
import org.springframework.web.servlet.View;

public interface WorkgroupViewFactory {

	WorkgroupView createWorkgroupView(Long workgroupId);

}
