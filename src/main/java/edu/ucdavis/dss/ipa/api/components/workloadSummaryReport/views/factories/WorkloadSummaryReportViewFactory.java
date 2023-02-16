package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import java.util.concurrent.CompletableFuture;

public interface WorkloadSummaryReportViewFactory {
    WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupId, long year);

    CompletableFuture<byte[]> createWorkloadSummaryReportBytes(long[] workgroupId, long year);
}
