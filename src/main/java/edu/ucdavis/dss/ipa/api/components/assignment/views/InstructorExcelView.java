package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.entities.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class InstructorExcelView extends AbstractXlsView {
    private AssignmentView.InstructorView instructorViewDTO = null;

    public InstructorExcelView(AssignmentView.InstructorView instructorViewDTO) {
        this.instructorViewDTO = instructorViewDTO;
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
        for(Instructor instructor : instructorViewDTO.getInstructors()) {
            Row excelHeader = sheet.createRow(row);
            int col = 0;
            excelHeader.createCell(col).setCellValue(instructor.getFullName());
            col++;
            for(Term term : instructorViewDTO.getTerms()) {
                List<TeachingAssignment> teachingAssignments = this.getApprovedTeachingAssignmentsByInstructorAndTermCode(instructor, term.getTermCode());
                String assignmentsList = "";

                if (teachingAssignments.size() > 0) {
                    for (TeachingAssignment teachingAssignment : teachingAssignments) {
                        String subjectCode = teachingAssignment.getSectionGroup().getCourse().getSubjectCode();
                        String courseNumber = teachingAssignment.getSectionGroup().getCourse().getCourseNumber();
                        String assignmentDescription = subjectCode + " " + courseNumber;

                        if (assignmentsList.length() > 0) {
                            assignmentsList += ", ";
                        }

                        assignmentsList += assignmentDescription;
                    }

                    excelHeader.createCell(col).setCellValue(assignmentsList);
                    col++;
                } else {
                    excelHeader.createCell(col).setCellValue("");
                    col++;
                }
            }

            row++;
        }
    }

    private List<TeachingAssignment> getApprovedTeachingAssignmentsByInstructorAndTermCode(Instructor instructor, String termCode) {
        List<TeachingAssignment> teachingAssignments = new ArrayList<>();

        for (TeachingAssignment teachingAssignment : instructor.getTeachingAssignments()) {
            if (teachingAssignment.isApproved() == true && termCode.equals(teachingAssignment.getTermCode())) {
                teachingAssignments.add(teachingAssignment);
            }
        }

        return teachingAssignments;
    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);
        int col = 0;

        excelHeader.createCell(col).setCellValue("Instructor");
        col++;

        for(Term term : instructorViewDTO.getTerms()) {
            excelHeader.createCell(col).setCellValue(Term.getRegistrarName(term.getTermCode()));
            col++;
        }
    }
}
