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

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {

        String filename = "attachment; filename=\"" + this.year + " Workload Summary Report.xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        Sheet worksheet = workbook.createSheet("Raw Assignments Data");

        for (WorkloadInstructorDTO workloadInstructor : workloadInstructorDTOList) {
            buildSheet(worksheet, workloadInstructor);
        }

        ExcelHelper.expandHeaders(workbook);
    }

    private void buildSheet(Sheet worksheet, WorkloadInstructorDTO workloadInstructor) {
        ExcelHelper.setSheetHeader(worksheet,
            Arrays.asList("Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description",
                "Offering"));

        ExcelHelper.writeRowToSheet(worksheet, Arrays.asList(
            yearToAcademicYear(this.year),
            workloadInstructor.getDepartment(),
            workloadInstructor.getInstructorType().toUpperCase(),
            workloadInstructor.getName(),
            workloadInstructor.getTerm(),
            workloadInstructor.getCourseType(),
            workloadInstructor.getDescription(),
            workloadInstructor.getOffering()
        ));
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2, 4);
    }
}
