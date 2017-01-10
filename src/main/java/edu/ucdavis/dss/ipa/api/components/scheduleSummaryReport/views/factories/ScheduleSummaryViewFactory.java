package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;

public interface ScheduleSummaryViewFactory {

    ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year, String termCode);

}
