package edu.ucdavis.dss.ipa.api.components.report.views.factories;

import org.javers.core.diff.Diff;

import java.util.List;

public interface ReportViewFactory {

	List<Diff> createDiffView(long workgroupId, long year, String termCode);

}
