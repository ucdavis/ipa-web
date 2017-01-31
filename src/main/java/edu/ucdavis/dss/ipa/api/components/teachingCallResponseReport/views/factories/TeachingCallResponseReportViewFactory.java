package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;

public interface TeachingCallResponseReportViewFactory {

    TeachingCallResponseReportView createTeachingCallResponseReportView(long workgroupId, long year);

}
