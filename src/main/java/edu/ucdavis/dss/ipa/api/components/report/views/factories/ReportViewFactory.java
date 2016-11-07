package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffView;

import java.util.List;

public interface ReportViewFactory {

	List<SectionDiffView> createDiffView(long workgroupId, long year, String termCode);
}
