package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import org.springframework.web.servlet.View;

public interface ScheduleSummaryViewFactory {

    ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year, String termCode, boolean simpleView);

    View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode);

    View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode, boolean simpleView);
}
