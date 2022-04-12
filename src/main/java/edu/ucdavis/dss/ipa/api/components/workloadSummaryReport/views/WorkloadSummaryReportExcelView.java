package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

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
    private List<WorkloadInstructorDTO> workloadInstructorDTOList;

    public WorkloadSummaryReportExcelView(List<WorkloadInstructorDTO> workloadInstructorDTOList, long year) {
        this.workloadInstructorDTOList = workloadInstructorDTOList;
        this.year = year;
    }

    public static void buildRawAssignmentsSheet(Workbook wb, List<WorkloadInstructorDTO> workloadInstructorDTOList) {
        Sheet worksheet = wb.createSheet("Raw Assignments Data");

        ExcelHelper.setSheetHeader(worksheet,
            Arrays.asList("Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description",
                "Offering", "Enrollment", "Planned Seats", "Previous Enrollment (YoY)",
                "Previous Enrollment (Last Offered)", "Units", "SCH", "Note"));

        for (WorkloadInstructorDTO workloadInstructor : workloadInstructorDTOList) {
            ExcelHelper.writeRowToSheet(worksheet, workloadInstructor.toList());
        }
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {

        String filename = "attachment; filename=\"" + this.year + " Workload Summary Report.xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        buildRawAssignmentsSheet(workbook, workloadInstructorDTOList);
        ExcelHelper.expandHeaders(workbook);
    }
}
