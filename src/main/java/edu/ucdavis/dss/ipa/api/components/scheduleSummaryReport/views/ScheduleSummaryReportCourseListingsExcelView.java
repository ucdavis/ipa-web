package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

public class ScheduleSummaryReportCourseListingsExcelView extends AbstractXlsView {
    private List<ScheduleSummaryReportView> reportViews;

    public ScheduleSummaryReportCourseListingsExcelView(
        List<ScheduleSummaryReportView> reportViews) {
        this.reportViews = reportViews;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        // Set up file
        final Long year = reportViews.stream().findFirst().get().getYear();
        String dateOfDownload = new Date().toString();
        String fileName = "attachment; filename=\"" + "Course Listings -" + yearToAcademicYear(year) +
                "-" + dateOfDownload + ".xls\"";
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", fileName);

        Sheet sheet = workbook.createSheet("Course Listings");
        setExcelHeader(sheet);

        int row = 1;

        // loop over each department report data
        for (ScheduleSummaryReportView reportView: reportViews) {
            List<Course> courses = reportView.getCourses();
            List<String> termCodes = Term.getQuarterTermCodesByYear(year);
            String workgroupName = "";

            if (reportView.getCourses().size() > 0) {
                workgroupName =
                    reportView.getCourses().get(0).getSchedule().getWorkgroup()
                        .getName();
            }

            for (String termCode : termCodes) {
                Collections.sort(courses, new Comparator<Course>() {
                    @Override
                    public int compare(Course course1, Course course2) {
                        if (course1.getSubjectCode().equals(course2.getSubjectCode())) {
                            if (course1.getCourseNumber().equals(course2.getCourseNumber())) {
                                // Compare by sequence pattern
                                return course1.getSequencePattern()
                                    .compareTo(course2.getSequencePattern());
                            }
                            // Compare by course number
                            return course1.getCourseNumber().compareTo(course2.getCourseNumber());
                        }
                        return course1.getSubjectCode().compareTo(course2.getSubjectCode());
                    }
                });

                for (Course course : courses) {
                    // Set course column
                    int col;
                    for (SectionGroup sectionGroup : course.getSectionGroups()) {
                        // Course will include sectionGroups from all terms in the year, but the view is scoped to a term
                        if (termCode.equals(sectionGroup.getTermCode()) == false) {
                            continue;
                        }
                        Row excelHeader = sheet.createRow(row);

                        col = 0;
                        excelHeader.createCell(col).setCellValue(yearToAcademicYear(year));

                        col = 1;
                        excelHeader.createCell(col).setCellValue(Term.getRegistrarName(termCode));

                        col = 2;
                        excelHeader.createCell(col).setCellValue(workgroupName);

                        col = 3;
                        excelHeader.createCell(col).setCellValue(course.getShortDescription());

                        col = 4;
                        excelHeader.createCell(col).setCellValue(course.getTitle());

                        // Set course values
                        for (Section section : sectionGroup.getSections()) {
                            // Set Sequence Pattern
                            col = 5;
                            excelHeader.createCell(col).setCellValue(section.getSequenceNumber());
                            // Set Units
                            col = 6;
                            excelHeader.createCell(col)
                                .setCellValue(sectionGroup.getDisplayUnits());
                            // Set Seats
                            col = 7;
                            if (section.getSeats() != null) {
                                excelHeader.createCell(col).setCellValue(section.getSeats());
                            }

                            for (Activity activity : sectionGroup.getActivities()) {
                                // Set Activity Type Code Description
                                col = 0;
                                excelHeader.createCell(col).setCellValue(yearToAcademicYear(year));

                                col = 1;
                                excelHeader.createCell(col)
                                    .setCellValue(Term.getRegistrarName(termCode));

                                col = 2;
                                excelHeader.createCell(col).setCellValue(workgroupName);

                                col = 3;
                                excelHeader.createCell(col)
                                    .setCellValue(course.getShortDescription());

                                col = 4;
                                excelHeader.createCell(col).setCellValue(course.getTitle());
                                col = 5;
                                excelHeader.createCell(col)
                                    .setCellValue(section.getSequenceNumber());

                                col = 8;
                                excelHeader.createCell(col)
                                    .setCellValue(activity.getActivityTypeCodeDescription());

                                row++;
                                excelHeader = sheet.createRow(row);
                            }
                            for (Activity activity : section.getActivities()) {
                                col = 0;
                                excelHeader.createCell(col).setCellValue(yearToAcademicYear(year));

                                col = 1;
                                excelHeader.createCell(col)
                                    .setCellValue(Term.getRegistrarName(termCode));

                                col = 2;
                                excelHeader.createCell(col).setCellValue(workgroupName);

                                col = 3;
                                excelHeader.createCell(col)
                                    .setCellValue(course.getShortDescription());

                                col = 4;
                                excelHeader.createCell(col).setCellValue(course.getTitle());
                                col = 5;
                                excelHeader.createCell(col)
                                    .setCellValue(section.getSequenceNumber());

                                // Set Activity Type Code Description
                                col = 8;
                                excelHeader.createCell(col)
                                    .setCellValue(activity.getActivityTypeCodeDescription());

                                row++;
                                excelHeader = sheet.createRow(row);
                            }
                            if (section.getActivities().size() == 0) {
                                continue;
                            }
                        }
                    }
                }
            }

            ExcelHelper.expandHeaders(workbook);
        }
    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);
        excelHeader.createCell(0).setCellValue("Academic Year");
        excelHeader.createCell(1).setCellValue("Quarter");
        excelHeader.createCell(2).setCellValue("Department");
        excelHeader.createCell(3).setCellValue("Course Code");
        excelHeader.createCell(4).setCellValue("Course Description");
        excelHeader.createCell(5).setCellValue("Section");
        excelHeader.createCell(6).setCellValue("Units");
        excelHeader.createCell(7).setCellValue("Seats");
        excelHeader.createCell(8).setCellValue("Activity");
    }

    private String yearToAcademicYear(long year) {
        return year + "-" + String.valueOf(year + 1).substring(2, 4);
    }
}