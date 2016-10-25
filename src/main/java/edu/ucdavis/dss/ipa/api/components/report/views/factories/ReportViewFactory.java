package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.ipa.api.components.report.views.DiffView;

public interface ReportViewFactory {

	DiffView createDiffView(long workgroupId, long year, String termCode);

}
