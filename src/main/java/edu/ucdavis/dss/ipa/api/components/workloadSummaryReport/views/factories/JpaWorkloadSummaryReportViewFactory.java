package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadHistoricalReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.WorkloadAssignmentService;
import edu.ucdavis.dss.ipa.services.WorkloadSnapshotService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaWorkloadSummaryReportViewFactory implements WorkloadSummaryReportViewFactory {
    @Inject
    ScheduleService scheduleService;

    @Inject
    WorkloadAssignmentService workloadAssignmentService;

    @Inject
    WorkloadSnapshotService workloadSnapshotService;

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long workloadSnapshotId) {
        WorkloadSnapshot snapshot = workloadSnapshotService.findById(workloadSnapshotId);

        return new WorkloadSummaryReportExcelView(snapshot.getWorkloadAssignments(), snapshot.getYear(), snapshot.getName(), true);
    }

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupIds, long year) {
        List<WorkloadAssignment> workloadAssignments = new ArrayList<>();

        for (long workgroupId : workgroupIds) {
            workloadAssignments.addAll(workloadAssignmentService.generateWorkloadAssignments(workgroupId, year));
        }

        return new WorkloadSummaryReportExcelView(workloadAssignments, year);
    }

    public WorkloadHistoricalReportExcelView createHistoricalWorkloadExcelView(long workgroupId, long year) {
        Map<Long, List<WorkloadAssignment>> workloadAssignmentsMap = new HashMap<>();

        for (long i = 0; i < 5; i++) {
            long slotYear = year - i;

            Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, slotYear);

            // skip years without a schedule
            if (schedule != null) {
                List<WorkloadAssignment> workloadAssignments =
                    new ArrayList<>(workloadAssignmentService.generateWorkloadAssignments(workgroupId, slotYear));

                workloadAssignmentsMap.put(slotYear, workloadAssignments);
            }
        }

        return new WorkloadHistoricalReportExcelView(workloadAssignmentsMap);
    }

    @Override
//    @Async
    @Transactional
    // needed for Async https://stackoverflow.com/questions/17278385/spring-async-generates-lazyinitializationexceptions
    public CompletableFuture<byte[]> createWorkloadSummaryReportBytes(long[] workgroupIds, long year) {
        List<WorkloadAssignment> workloadAssignments = new ArrayList<>();
        System.out.println("Generating workload report for " + workgroupIds.length + " departments");

        int count = 0;
        for (long workgroupId : workgroupIds) {
            ++count;
            System.out.println(count + ". Generating for workgroupId: " + workgroupId);

            workloadAssignments.addAll(workloadAssignmentService.generateWorkloadAssignments(workgroupId, year));
        }

        System.out.println("Finished gathering data, writing to excel");

        XSSFWorkbook workbook = new XSSFWorkbook();
        WorkloadSummaryReportExcelView.buildRawAssignmentsSheet(workbook, workloadAssignments);
        ExcelHelper.expandHeaders(workbook);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            workbook.write(bos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(bos.toByteArray());
    }
}
