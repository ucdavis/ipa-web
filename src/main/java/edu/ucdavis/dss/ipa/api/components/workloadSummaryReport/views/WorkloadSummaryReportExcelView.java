package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.WorkloadAssignment;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class WorkloadSummaryReportExcelView extends AbstractXlsxView {
    private long year;
    private List<WorkloadAssignment> workloadAssignments;
    private Boolean isSnapshot;

    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
        this.isSnapshot = false;
    }
    public WorkloadSummaryReportExcelView(List<WorkloadAssignment> workloadAssignments, long year, Boolean isSnapshot) {
        this.workloadAssignments = workloadAssignments;
        this.year = year;
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

        String filename = "attachment; filename=\"" + this.year + (this.isSnapshot ? " Workload Snapshot" : " Workload Summary Report") + ".xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildRawAssignmentsSheet(workbook, workloadAssignments);
        ExcelHelper.expandHeaders(workbook);
    }
}
