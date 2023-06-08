package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class WorkloadSummaryReportExcelView extends AbstractXlsxView {
    private long year;
    private List<WorkloadAssignment> workloadAssignments;
    private Boolean isSnapshot;
    private String snapshotName;

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.isSnapshot = false;
    }
    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year, String snapshotName, Boolean isSnapshot) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.snapshotName = snapshotName;
        this.isSnapshot = true;
    }

    public static void buildRawAssignmentsSheet(Workbook wb, List<WorkloadAssignment> workloadAssignments) {
        Sheet worksheet = wb.createSheet("Raw Assignments Data");

        ExcelHelper.setSheetHeader(worksheet,
            Arrays.asList("Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description",
                "Offering", "Enrollment", "Planned Seats", "Previous Enrollment (YoY)",
                "Previous Enrollment (Last Offered)", "Units", "SCH", "Note"));

        for (WorkloadAssignment workloadAssignment : workloadAssignments) {
            ExcelHelper.writeRowToSheet(worksheet, workloadAssignment.toList());
        }
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        String baseName = this.isSnapshot ? "Workload Snapshot - " + this.snapshotName : this.year + " Workload Summary Report";
        String filename = "attachment; filename=\"" + baseName + ".xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildRawAssignmentsSheet(workbook, orderByInstructorTypeAndName(workloadAssignments));
        ExcelHelper.expandHeaders(workbook);
    }

    private List<WorkloadAssignment> orderByInstructorTypeAndName(List<WorkloadAssignment> workloadAssignments) {
        return workloadAssignments.stream().sorted(
                Comparator.comparing(WorkloadAssignment::getInstructorType).thenComparing(WorkloadAssignment::getName))
            .collect(Collectors.toList());
    }
}
