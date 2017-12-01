package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TeachingCallResponseReportExcelView extends AbstractXlsView {
    private TeachingCallResponseReportView teachingCallResponseReportViewDTO = null;

    public TeachingCallResponseReportExcelView(TeachingCallResponseReportView teachingCallResponseReportViewDTO) {
        this.teachingCallResponseReportViewDTO = teachingCallResponseReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) {
        String filename = teachingCallResponseReportViewDTO.getSchedule().getWorkgroup().getName() + " - " + teachingCallResponseReportViewDTO.getSchedule().getYear() + " - TeachingCallResponseReport.xls";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // Create sheet
        Sheet sheet = workbook.createSheet("Teaching Preferences");

        Map<String, Integer> termCodeColumnMapping = new HashMap<String, Integer>();

        List<String> usedTermCodes = new ArrayList<String>();
        // Generate a list of all terms used in the teaching call receipts
        for(TeachingCallReceipt receipt : teachingCallResponseReportViewDTO.getTeachingCallReceipts()) {
            List<String> termCodes = receipt.getTermsBlobAsList();
            for(String termCode : termCodes) {
                if (usedTermCodes.contains(termCode) == false) {
                    usedTermCodes.add(termCode);
                }
            }
        }

        Collections.sort(usedTermCodes, String.CASE_INSENSITIVE_ORDER);

        // Produce the header row
        int currentRow = 0;
        Row row = sheet.createRow(currentRow);

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);

        Cell cell = row.createCell(0);
        cell.setCellValue("Instructor Last");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(1);
        cell.setCellValue("Instructor First");
        cell.setCellStyle(cellStyle);

        int currentCell = 2;

        for(String termCode : usedTermCodes) {
            cell = row.createCell(currentCell);
            cell.setCellValue(Term.getRegistrarName(termCode) + " " + Term.getYear(termCode));
            cell.setCellStyle(cellStyle);

            // Build the term code -> Excel column mapping for later use
            termCodeColumnMapping.put(termCode, currentCell);

            System.out.println("termCode " + termCode + " will be in cell " + currentCell);

            currentCell++;
        }

        List<TeachingAssignment> allTeachingAssignments = teachingCallResponseReportViewDTO.getTeachingAssignments();
        for(TeachingAssignment teachingAssignment : allTeachingAssignments) {
            System.out.println("Instructor ID: " + teachingAssignment.getInstructor().getId() + ", termCode: " + teachingAssignment.getTermCode() + ", fromInstructor: " + teachingAssignment.isFromInstructor());
        }

        // Produce each row after the header row
        int lastInstructorStartRow = 1;
        int lastInstructorRowCount = 0;

        List<Instructor> sortedInstructors = teachingCallResponseReportViewDTO.getInstructors();
        Collections.sort(sortedInstructors, (a, b) -> a.getLastName().compareTo(b.getLastName()));

        for(Instructor instructor : sortedInstructors) {
            int instructorStartRow = lastInstructorStartRow + lastInstructorRowCount;
            currentRow = instructorStartRow;

            System.out.println("Starting for instructor: " + instructor.getFullName());
            System.out.println("\tInstructorStartRow: " + instructorStartRow);

            lastInstructorStartRow = instructorStartRow;
            lastInstructorRowCount = 1;

            row = sheet.getRow(currentRow);
            if(row == null) {
                row = sheet.createRow(currentRow);
            }

            // Instructor last, instructor first
            row.createCell(0).setCellValue(instructor.getLastName());
            row.createCell(1).setCellValue(instructor.getFirstName());

            for(String termCode : usedTermCodes) {
                currentRow = instructorStartRow;
                currentCell = termCodeColumnMapping.get(termCode);

                row = sheet.getRow(currentRow);
                if(row == null) {
                    row = sheet.createRow(currentRow);
                }

                System.out.println("\tProcessing for termCode " + termCode + ". Will start at row " + currentRow + " and cell " + currentCell);

                List<TeachingAssignment> teachingAssignments = allTeachingAssignments.stream()
                        .filter(
                                assignment -> assignment.getInstructor().getId() == instructor.getId()
                                && assignment.getTermCode().equals(termCode)
                                && assignment.isFromInstructor()
                        ).collect(Collectors.toList());

                System.out.println("\t" + instructor.getFullName() + " in " + termCode + " has " + teachingAssignments.size() + " assignments");
                for(TeachingAssignment teachingAssignment : teachingAssignments) {
                    System.out.println("\t\tWriting (ID: " + teachingAssignment.getId() + ") " + describeTeachingAssignment(teachingAssignment) + " in row " + currentRow + ", cell " + currentCell);
                    row.createCell(currentCell).setCellValue(describeTeachingAssignment(teachingAssignment));

                    currentRow++;
                    row = sheet.getRow(currentRow);
                    if(row == null) {
                        row = sheet.createRow(currentRow);
                    }
                }

                if(teachingAssignments.size() > lastInstructorRowCount) { lastInstructorRowCount = teachingAssignments.size(); }
            }
        }

        // Auto-size the columns to fit contents
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        int column = 2;
        for(column = 2; column < 2 + usedTermCodes.size(); column++) {
            sheet.autoSizeColumn(column);
        }
    }

    private String describeTeachingAssignment(TeachingAssignment teachingAssignment) {
        if(teachingAssignment.isBuyout()) return "Buyout";
        if(teachingAssignment.isCourseRelease()) return "Course Release";
        if(teachingAssignment.isSabbatical()) return "Sabbatical";
        if(teachingAssignment.isInResidence()) return "In Residence";
        if(teachingAssignment.isWorkLifeBalance()) return "Work-life Balance";
        if(teachingAssignment.isLeaveOfAbsence()) return "Leave of Absence";

        SectionGroup sg = teachingAssignment.getSectionGroup();
        Course course = sg.getCourse();

        return course.getSubjectCode() + " " + course.getCourseNumber() + " " + course.getSequencePattern();
    }

}
