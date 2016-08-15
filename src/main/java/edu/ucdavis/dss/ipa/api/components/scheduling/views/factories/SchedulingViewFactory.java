package edu.ucdavis.dss.ipa.api.components.scheduling.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;

public interface SchedulingViewFactory {

	SchedulingView createSchedulingView(long workgroupId, long year, String termCode, Boolean showDoNotPrint);

}
