package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

public class ScheduleSummaryReportExcelView extends AbstractXlsView {
    private ScheduleSummaryReportView scheduleSummaryReportViewDTO = null;

    public ScheduleSummaryReportExcelView(ScheduleSummaryReportView scheduleSummaryReportViewDTO) {
        this.scheduleSummaryReportViewDTO = scheduleSummaryReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String shortTermCode = scheduleSummaryReportViewDTO.getTermCode();
        Long year = scheduleSummaryReportViewDTO.getYear();
        String termCode = "";

        if (Long.valueOf(shortTermCode) > 4) {
            termCode = year + shortTermCode;
        } else {
            year = Long.valueOf(year) + 1;
            termCode = year + shortTermCode;
        }

        String workgroupName = "";
        if (scheduleSummaryReportViewDTO.getCourses().size() > 0) {
            workgroupName = scheduleSummaryReportViewDTO.getCourses().get(0).getSchedule().getWorkgroup().getName();
        }

        String dateOfDownload = new Date().toString();
        String fileName = "attachment; filename=" + workgroupName + "-" + termCode + "-schedule_summary-" + dateOfDownload + ".xls";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", fileName);

        // Create sheet
        Sheet sheet = workbook.createSheet("Schedule");

        setExcelHeader(sheet);

        int row = 1;

        List<Course> courses = scheduleSummaryReportViewDTO.getCourses();

        // Sort courses by subjectCode, course number, and sequence pattern
        Collections.sort(courses, new Comparator<Course>() {
            @Override
            public int compare(Course course1, Course course2) {
                if (course1.getSubjectCode().equals(course2.getSubjectCode())) {
                    if (course1.getCourseNumber().equals(course2.getCourseNumber())) {
                        // Compare by sequence pattern
                        return course1.getSequencePattern().compareTo(course2.getSequencePattern());
                    }

                    // Compare by course number
                    return course1.getCourseNumber().compareTo(course2.getCourseNumber());
                }

                return course1.getSubjectCode().compareTo(course2.getSubjectCode());
            }
        });

        for(Course course : courses) {

            // Set course column
            int col = 0;

            // Only record this course if it has a 'Visible' Section with the relevant termCode
            boolean shouldRecordCourse = false;

            for (SectionGroup sectionGroup : course.getSectionGroups()) {
                if (termCode.equals(sectionGroup.getTermCode())) {

                    for (Section section : sectionGroup.getSections()) {
                        if (section.isVisible() == null || section.isVisible() == true) {
                            shouldRecordCourse = true;
                        }
                    }
                }
            }

            if (shouldRecordCourse == true) {

                Row excelHeader = sheet.createRow(row);
                excelHeader.createCell(col).setCellValue(course.getShortDescription());

                for (SectionGroup sectionGroup : course.getSectionGroups()) {
                    // Course will include sectionGroups from all terms in the year, but our view is scoped to a term
                    if (termCode.equals(sectionGroup.getTermCode()) == false) {
                        continue;
                    }

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
                            excelHeader.createCell(col).setCellValue(activity.getDayIndicatorDescription());

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
        excelHeader.createCell(6).setCellValue("Days");
        excelHeader.createCell(7).setCellValue("Start");
        excelHeader.createCell(8).setCellValue("End");
        excelHeader.createCell(9).setCellValue("Location");
    }
}
