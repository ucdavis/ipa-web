package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.ipa.api.components.report.views.DiffView;

import java.util.List;

public interface ReportViewFactory {

	List<DiffView> createDiffView(long workgroupId, long year, String termCode);

}
