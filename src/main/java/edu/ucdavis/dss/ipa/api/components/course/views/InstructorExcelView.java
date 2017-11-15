package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InstructorExcelView extends AbstractXlsView {
    private InstructorView instructorViewDTO = null;

    public InstructorExcelView(InstructorView instructorViewDTO) {
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

            excelHeader.createCell(0).setCellValue(instructor.getFullName());

            int col = 2;
            for(Term term : instructorViewDTO.getTerms()) {
                List<TeachingAssignment> teachingAssignments = this.getTeachingAssignmentsByInstructorAndTermCode(instructor, term.getTermCode());

                if (teachingAssignments.size() > 0) {
                    // TODO: Loop over teachingAssignments, and add the subj + course number together, and concatenate to comma separated string
//                    excelHeader.createCell(col).setCellValue(sectionGroup.getPlannedSeats());
                } else {
                    excelHeader.createCell(col).setCellValue("");
                }

                col++;
            }

            row++;
        }
    }

    private List<TeachingAssignment> getTeachingAssignmentsByInstructorAndTermCode(Instructor instructor, String termCode) {

    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);

        excelHeader.createCell(0).setCellValue("Course");
        excelHeader.createCell(1).setCellValue("Tracks");

        int col = 2;
        for(Term term : instructorViewDTO.getTerms()) {
            excelHeader.createCell(col).setCellValue(Term.getRegistrarName(term.getTermCode()));
            col++;
        }
    }

    /*
    private SectionGroup  getSectionGroupByCourseAndTermCode(Course course, String termCode) {
        Predicate<SectionGroup> predicate = sg-> sg.getTermCode().equals(termCode) && sg.getCourse().equals(course);
        String courseDesc = course.getShortDescription();
//        List<SectionGroup> matchingSectionGroups = courseViewDTO.getSectionGroups().stream().filter(predicate).collect(Collectors.toList());
        List<SectionGroup> matchingSectionGroups = course.getSectionGroups().stream().filter(predicate).collect(Collectors.toList());
        if (matchingSectionGroups.size() > 0) {
            return matchingSectionGroups.get(0);
        } else {
            return null;
        }
    }

    */
}
