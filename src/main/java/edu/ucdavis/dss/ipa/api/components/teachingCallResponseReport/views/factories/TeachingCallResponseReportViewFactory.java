package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import org.springframework.web.servlet.View;

public interface TeachingCallResponseReportViewFactory {
    TeachingCallResponseReportView createTeachingCallResponseReportView(long workgroupId, long year);

    View createTeachingCallResponseReportExcelView(long workgroupId, long year);
}
