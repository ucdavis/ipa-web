package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views;

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

public class WorkloadHistoricalReportExcelView extends AbstractXlsxView {
    private Map<Long, List<InstructorAssignment>> instructorAssignmentsMap;

    public WorkloadHistoricalReportExcelView(Map<Long, List<InstructorAssignment>> instructorAssignmentsMap) {
        this.instructorAssignmentsMap = instructorAssignmentsMap;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        List<Long> years = this.instructorAssignmentsMap.keySet().stream().sorted(Comparator.reverseOrder()).collect(
            Collectors.toList());

        String filename = "attachment; filename=\"" + years.get(0) + " Workload Historical Report.xlsx";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        for (Long year : years) {
            Sheet worksheet = workbook.createSheet(String.valueOf(year));

            ExcelHelper.setSheetHeader(worksheet,
                Arrays.asList("Year", "Department", "Instructor Type", "Name", "Term", "Course Type", "Description",
                    "Offering", "Enrollment", "Planned Seats", "Previous Enrollment (YoY)",
                    "Previous Enrollment (Last Offered)", "Units", "SCH", "Note"));

            for (InstructorAssignment instructorAssignment : instructorAssignmentsMap.get(year)) {
                ExcelHelper.writeRowToSheet(worksheet, instructorAssignment.toList());
            }
        }

        ExcelHelper.expandHeaders(workbook);
    }
}
