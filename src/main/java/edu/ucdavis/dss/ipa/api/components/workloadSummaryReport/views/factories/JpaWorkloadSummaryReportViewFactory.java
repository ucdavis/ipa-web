package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.ScheduleInstructorNoteService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkloadAssignmentService;
import edu.ucdavis.dss.ipa.services.WorkloadSnapshotService;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaWorkloadSummaryReportViewFactory implements WorkloadSummaryReportViewFactory {
    @Inject
    WorkloadAssignmentService workloadAssignmentService;

    @Inject
    WorkloadSnapshotService workloadSnapshotService;

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long workloadSnapshotId) {
        WorkloadSnapshot snapshot = workloadSnapshotService.findById(workloadSnapshotId);

        // write data to excel
        WorkloadSummaryReportExcelView workloadSummaryReportExcelView =
            new WorkloadSummaryReportExcelView(snapshot.getWorkloadAssignments(), snapshot.getYear(), true);

        return workloadSummaryReportExcelView;
    }

    @Override
    public WorkloadSummaryReportExcelView createWorkloadSummaryReportExcelView(long[] workgroupIds, long year) {
        List<WorkloadAssignment> workloadAssignments = new ArrayList<>();

        for (long workgroupId : workgroupIds) {
            workloadAssignments.addAll(workloadAssignmentService.generateWorkloadAssignments(workgroupId, year));
        }

        // write data to excel
        WorkloadSummaryReportExcelView workloadSummaryReportExcelView =
            new WorkloadSummaryReportExcelView(workloadAssignments, year);

        return workloadSummaryReportExcelView;
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
