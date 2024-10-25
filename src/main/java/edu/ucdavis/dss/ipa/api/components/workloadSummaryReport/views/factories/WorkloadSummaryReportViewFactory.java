package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadHistoricalReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface WorkloadSummaryReportViewFactory {
    WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long workloadSnapshotId);

    WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupId, long year);

    WorkloadHistoricalReportExcelView createHistoricalWorkloadExcelView(long workgroupId, long year);

    CompletableFuture<byte[]> createWorkloadSummaryReportBytes(long[] workgroupId, long year);
    CompletableFuture<byte[]> createWorkloadSummaryReportBytes(
        Map<Long, Map<String, Map<String, Long>>> departmentSnapshots, long year);
}
