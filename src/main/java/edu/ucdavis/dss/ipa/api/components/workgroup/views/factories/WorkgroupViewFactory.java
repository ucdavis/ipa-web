package edu.ucdavis.dss.ipa.api.components.workgroup.views.factories;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;

public interface WorkgroupViewFactory {

	WorkgroupView createWorkgroupView(Long workgroupId);

}
