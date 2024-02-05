package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by okadri on 9/14/16.
 */
public class CourseExcelView extends AbstractXlsView {
    private CourseView courseViewDTO = null;
    private Workgroup workgroup;
    private long year;

    public CourseExcelView(CourseView courseViewDTO, Workgroup workgroup, long year) {
        this.courseViewDTO = courseViewDTO;
        this.workgroup = workgroup;
        this.year = year;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=" + Term.getAcademicYearFromYear(year) + " " + workgroup.getName() + " Schedule Data.xls");

        // Create sheet
        Sheet sheet = workbook.createSheet("Schedule");

        setExcelHeader(sheet);

        int row = 1;
        for(Course course : courseViewDTO.getCourses()) {
            Row excelHeader = sheet.createRow(row);

            excelHeader.createCell(0).setCellValue(course.getShortDescription());
            List<String> tagNameList = course.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            excelHeader.createCell(1).setCellValue(StringUtils.join(tagNameList, ','));

            int col = 2;
            for(Term term : courseViewDTO.getTerms()) {
                SectionGroup sectionGroup = this.getSectionGroupByCourseAndTermCode(course, term.getTermCode());

                if (sectionGroup != null) {
                    if (sectionGroup.getPlannedSeats() != null) {
                        excelHeader.createCell(col).setCellValue(sectionGroup.getPlannedSeats());
                    } else {
                        excelHeader.createCell(col).setCellValue(0);
                    }
                }

                col++;
            }

            row++;
        }
    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);

        excelHeader.createCell(0).setCellValue("Course");
        excelHeader.createCell(1).setCellValue("Tracks");

        int col = 2;
        for(Term term : courseViewDTO.getTerms()) {
            excelHeader.createCell(col).setCellValue(Term.getRegistrarName(term.getTermCode()));
            col++;
        }
    }

    private SectionGroup  getSectionGroupByCourseAndTermCode(Course course, String termCode) {
        Predicate<SectionGroup> predicate = sg-> sg.getTermCode().equals(termCode) && sg.getCourse().equals(course);
        List<SectionGroup> matchingSectionGroups = course.getSectionGroups().stream().filter(predicate).collect(Collectors.toList());

        if (matchingSectionGroups.size() > 0) {
            return matchingSectionGroups.get(0);
        } else {
            return null;
        }
    }

}
