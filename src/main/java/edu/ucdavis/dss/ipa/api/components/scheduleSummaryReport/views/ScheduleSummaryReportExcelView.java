package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ScheduleSummaryReportExcelView extends AbstractXlsView {
    private ScheduleSummaryReportView scheduleSummaryReportViewDTO = null;

    public ScheduleSummaryReportExcelView(ScheduleSummaryReportView scheduleSummaryReportViewDTO) {
        this.scheduleSummaryReportViewDTO = scheduleSummaryReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=ScheduleData.xls");

        // Create sheet
        Sheet sheet = workbook.createSheet("Schedule");

        setExcelHeader(sheet);

        int row = 1;

        for(Course course : scheduleSummaryReportViewDTO.getCourses()) {

            // Set course column
            int col = 0;

            Row excelHeader = sheet.createRow(row);
            excelHeader.createCell(col).setCellValue(course.getShortDescription());

            for (SectionGroup sectionGroup : course.getSectionGroups()) {

                // Set Assigned column
                col = 1;

                for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
                    if (teachingAssignment.isApproved()) {
                        excelHeader.createCell(col).setCellValue(teachingAssignment.getInstructor().getFullName());
                        break;
                    }
                }

                for (Section section : sectionGroup.getSections()) {

                    // Set Sequence Pattern
                    col = 2;
                    excelHeader.createCell(col).setCellValue(section.getSequenceNumber());

                    // Set CRN
                    col = 3;
                    excelHeader.createCell(col).setCellValue(section.getCrn());

                    // Set Seats
                    col = 4;
                    if (section.getSeats() != null) {
                        excelHeader.createCell(col).setCellValue(section.getSeats());
                    }

                    for (Activity activity : section.getActivities()) {
                        // Set Activity Type Code Description
                        col = 5;
                        excelHeader.createCell(col).setCellValue(activity.getActivityTypeCodeDescription());

                        // Set Days
                        col = 6;
                        excelHeader.createCell(col).setCellValue(activity.getDayIndicator());

                        // Set Start
                        if (activity.getStartTime() != null) {
                            col = 7;
                            excelHeader.createCell(col).setCellValue(activity.getStartTime().toString());
                        }

                        // Set Start
                        if (activity.getEndTime() != null) {
                            col = 8;
                            excelHeader.createCell(col).setCellValue(activity.getEndTime().toString());
                        }

                        if (activity.getLocationDescription() != null) {
                            col = 9;
                            excelHeader.createCell(col).setCellValue(activity.getLocationDescription());
                        }

                        row++;
                        excelHeader = sheet.createRow(row);
                    }

                    if (section.getActivities().size() == 0) {
                        row++;
                        excelHeader = sheet.createRow(row);
                    }
                }
            }

            if (course.getSectionGroups().size() == 0) {
                row++;
            }
        }
    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);

        excelHeader.createCell(0).setCellValue("Course");
        excelHeader.createCell(1).setCellValue("Assigned");
        excelHeader.createCell(2).setCellValue("Section");
        excelHeader.createCell(3).setCellValue("CRN");
        excelHeader.createCell(4).setCellValue("Seats");
        excelHeader.createCell(5).setCellValue("Activity");
        excelHeader.createCell(6).setCellValue("Activity");
        excelHeader.createCell(7).setCellValue("Start");
        excelHeader.createCell(8).setCellValue("End");
        excelHeader.createCell(9).setCellValue("Location");
    }
}
