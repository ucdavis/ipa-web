package edu.ucdavis.dss.ipa.api.components.scheduling.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingViewSectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

import java.util.List;

public interface SchedulingViewFactory {

	SchedulingView createSchedulingView(long workgroupId, long year, String termCode);

}
