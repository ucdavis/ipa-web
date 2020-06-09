package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ScheduleSummaryReportExcelView extends AbstractXlsView {
    private ScheduleSummaryReportView scheduleSummaryReportViewDTO = null;
    public ScheduleSummaryReportExcelView(ScheduleSummaryReportView scheduleSummaryReportViewDTO) {
        this.scheduleSummaryReportViewDTO = scheduleSummaryReportViewDTO;
    }
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Course> courses = scheduleSummaryReportViewDTO.getCourses();
        Set<String> shortTermCodes = new java.util.HashSet<String>();
        Long year = scheduleSummaryReportViewDTO.getYear();

        String shortTermCode = scheduleSummaryReportViewDTO.getTermCode();
        if(shortTermCode != null){
            String fullTermCode = "";
            if (Long.valueOf(shortTermCode) > 4) {
                fullTermCode = year + shortTermCode;
            } else {
                year = Long.valueOf(year) + 1;
                fullTermCode = year + shortTermCode;
            }
            shortTermCodes.add(fullTermCode);
        } else{
            for(Course course : courses) {
                for (SectionGroup sectionGroup : course.getSectionGroups()) {
                    shortTermCodes.add(sectionGroup.getTermCode());
                }
            }
        }

        String workgroupName = "";
        if (scheduleSummaryReportViewDTO.getCourses().size() > 0) {
            workgroupName = scheduleSummaryReportViewDTO.getCourses().get(0).getSchedule().getWorkgroup().getName();
        }
        String dateOfDownload = new Date().toString();
        String fileName = "attachment; filename=\"" + workgroupName + "-" + year + "-schedule_summary-" + dateOfDownload + ".xls\"";
        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", fileName);
        for(String termCode : shortTermCodes){
            // Create sheet
            Sheet sheet = workbook.createSheet(Term.getRegistrarName(termCode));
            setExcelHeader(sheet);
            sheet.setColumnWidth(0, 9000);
            int row = 1;
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
                for (SectionGroup sectionGroup : course.getSectionGroups()) {
                    // Course will include sectionGroups from all terms in the year, but the view is scoped to a term
                    if (termCode.equals(sectionGroup.getTermCode()) == false) {
                        continue;
                    }
                    Row excelHeader = sheet.createRow(row);
                    excelHeader.createCell(col).setCellValue(course.getShortDescription() + " - " + course.getTitle());
                    // Set Instructors column
                    col = 1;
                    for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
                        if (teachingAssignment.isApproved() && teachingAssignment.getInstructor() != null) {
                            excelHeader.createCell(col).setCellValue(teachingAssignment.getInstructor().getFullName());
                            break;
                        }
                    }
                    // Set TAs column
                    col = 2;
                    String taNames = "";
                    for (SupportAssignment supportAssignment : sectionGroup.getSupportAssignments()) {
                        if (supportAssignment.getAppointmentType().equals("teachingAssistant") == false || supportAssignment.getSupportStaff() == null) { continue; }
                        String displayName = supportAssignment.getSupportStaff().getFullName();
                        taNames += displayName;
                    }
                    String formattedTAs = String.join(", ", taNames);
                    excelHeader.createCell(col).setCellValue(formattedTAs);
                    // Set course values
                    for (Section section : sectionGroup.getSections()) {
                        // Set Sequence Pattern
                        col = 3;
                        excelHeader.createCell(col).setCellValue(section.getSequenceNumber());
                        // Set CRN
                        col = 4;
                        excelHeader.createCell(col).setCellValue(section.getCrn());
                        // Set Seats
                        col = 5;
                        if (section.getSeats() != null) {
                            excelHeader.createCell(col).setCellValue(section.getSeats());
                        }
                        // Set section TAs
                        col = 6;
                        String sectionTAs = "";
                        for (SupportAssignment supportAssignment : section.getSupportAssignments()) {
                            if (supportAssignment.getAppointmentType().equals("teachingAssistant") == false || supportAssignment.getSupportStaff() == null) { continue; }
                            String displayName = supportAssignment.getSupportStaff().getFirstName() + " " + supportAssignment.getSupportStaff().getLastName();
                            if (sectionTAs.length() > 0) {
                                sectionTAs += ", ";
                            }
                            sectionTAs += displayName;
                        }
                        excelHeader.createCell(col).setCellValue(sectionTAs);
                        for (Activity activity : sectionGroup.getActivities()) {
                            // Set Activity Type Code Description
                            col = 7;
                            excelHeader.createCell(col).setCellValue(activity.getActivityTypeCodeDescription());
                            // Set Days
                            col = 8;
                            excelHeader.createCell(col).setCellValue(activity.getDayIndicatorDescription());
                            // Set Start
                            if (activity.getStartTime() != null) {
                                col = 9;
                                excelHeader.createCell(col).setCellValue(activity.getStartTime().toString());
                            }
                            // Set Start
                            if (activity.getEndTime() != null) {
                                col = 10;
                                excelHeader.createCell(col).setCellValue(activity.getEndTime().toString());
                            }
                            if (activity.getLocationDescription() != null) {
                                col = 11;
                                excelHeader.createCell(col).setCellValue(activity.getLocationDescription());
                            }
                            row++;
                            excelHeader = sheet.createRow(row);
                        }
                        for (Activity activity : section.getActivities()) {
                            // Set Activity Type Code Description
                            col = 7;
                            excelHeader.createCell(col).setCellValue(activity.getActivityTypeCodeDescription());
                            // Set Days
                            col = 8;
                            excelHeader.createCell(col).setCellValue(activity.getDayIndicatorDescription());
                            // Set Start
                            if (activity.getStartTime() != null) {
                                col = 9;
                                excelHeader.createCell(col).setCellValue(activity.getStartTime().toString());
                            }
                            // Set Start
                            if (activity.getEndTime() != null) {
                                col = 10;
                                excelHeader.createCell(col).setCellValue(activity.getEndTime().toString());
                            }
                            if (activity.getLocationDescription() != null) {
                                col = 11;
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
        excelHeader.createCell(1).setCellValue("Instructors");
        excelHeader.createCell(2).setCellValue("TAs");
        excelHeader.createCell(3).setCellValue("Section");
        excelHeader.createCell(4).setCellValue("CRN");
        excelHeader.createCell(5).setCellValue("Seats");
        excelHeader.createCell(6).setCellValue("Section TAs");
        excelHeader.createCell(7).setCellValue("Activity");
        excelHeader.createCell(8).setCellValue("Days");
        excelHeader.createCell(9).setCellValue("Start");
        excelHeader.createCell(10).setCellValue("End");
        excelHeader.createCell(11).setCellValue("Location");
    }
}