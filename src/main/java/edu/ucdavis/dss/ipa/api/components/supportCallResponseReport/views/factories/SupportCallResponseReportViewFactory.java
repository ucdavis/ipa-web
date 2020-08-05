package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.SupportCallResponseReportView;
import org.springframework.web.servlet.View;

public interface SupportCallResponseReportViewFactory {
    SupportCallResponseReportView createSupportCallResponseReportView(long workgroupId, long year, String termShortCode);

    View createSupportCallResponseReportExcelView(long workgroupId, long year, String termShortCode);
}
