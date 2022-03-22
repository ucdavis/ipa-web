package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;

public interface WorkloadSummaryReportViewFactory {
    WorkloadSummaryReportView createWorkloadSummaryReportView(long workgroupId, long year);

    WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupId, long year);
}
