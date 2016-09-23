package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by okadri on 9/14/16.
 */
public class AssignmentExcelView extends AbstractXlsView {
    private Schedule schedule = null;
    private List<Instructor> instructors = new ArrayList<>();
    private List<ScheduleTermState> scheduleTermStates = new ArrayList<>();

    public AssignmentExcelView(Schedule schedule, List<Instructor> instructors, List<ScheduleTermState> scheduleTermStates) {
        this.schedule = schedule;
        this.instructors = instructors;
        this.scheduleTermStates = scheduleTermStates;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=ScheduleData.xls");

        // Create sheets
        Sheet byCourseSheet = workbook.createSheet("By Course");
        Sheet byInstructorSheet = workbook.createSheet("By Instructor");

        setExcelHeader(byCourseSheet, "Course");
        setExcelHeader(byInstructorSheet, "Instructor");

        // By Course Sheet
        int row = 1;
        for(Course course : schedule.getCourses()) {
            Row excelHeader = byCourseSheet.createRow(row);

            excelHeader.createCell(0).setCellValue(course.getShortDescription());
            List<String> tagNameList = course.getTags().stream().map(Tag::getName).collect(Collectors.toList());
            excelHeader.createCell(1).setCellValue(StringUtils.join(tagNameList, ','));

            int col = 2;
            for(ScheduleTermState state : scheduleTermStates) {
                SectionGroup sectionGroup = this.getSectionGroupByCourseAndTermCode(course, state.getTermCode());

                if (sectionGroup != null) {
                    excelHeader.createCell(col).setCellValue(
                            StringUtils.join(
                                    sectionGroup.getTeachingAssignments()
                                            .stream()
                                            .filter(TeachingAssignment::isApproved)
                                            .map(ta -> ta.getInstructor().getLastName() + " " + ta.getInstructor().getFirstName().charAt(0))
                                            .collect(Collectors.toList())
                                    , ", "
                            )
                    );
                }

                col++;
            }

            row++;
        }

        // By Instructor Sheet
        row = 1;
        for(Instructor instructor : instructors) {
            Row excelHeader = byInstructorSheet.createRow(row);

            excelHeader.createCell(0).setCellValue(instructor.getLastName() + ", " + instructor.getFirstName());

            int col = 1;
            for(ScheduleTermState state : scheduleTermStates) {
                excelHeader.createCell(col).setCellValue(
                        StringUtils.join(
                                instructor.getTeachingAssignments().stream()
                                        .filter(ta -> ta.isApproved() && ta.getSectionGroup() != null && state.getTermCode().equals(ta.getTermCode()))
                                        .map(ta -> ta.getSectionGroup().getCourse().getShortDescription())
                                        .collect(Collectors.toList())
                                , ", "
                        )
                );

                col++;
            }

            row++;
        }
    }

    private void setExcelHeader(Sheet excelSheet, String firstHeader) {
        Row excelHeader = excelSheet.createRow(0);
        int col = 0;

        excelHeader.createCell(col++).setCellValue(firstHeader);

        if ("Course".equals(firstHeader)) {
            excelHeader.createCell(col++).setCellValue("Tracks");
        }

        for(ScheduleTermState state : scheduleTermStates) {
            excelHeader.createCell(col).setCellValue(Term.getRegistrarName(state.getTermCode()));
            col++;
        }
    }

    private SectionGroup  getSectionGroupByCourseAndTermCode(Course course, String termCode) {
        Predicate<SectionGroup> predicate = sg-> sg.getTermCode().equals(termCode) && sg.getCourse().equals(course);
        String courseDesc = course.getShortDescription();
        List<SectionGroup> matchingSectionGroups = course.getSectionGroups().stream().filter(predicate).collect(Collectors.toList());
        if (matchingSectionGroups.size() > 0) {
            return matchingSectionGroups.get(0);
        } else {
            return null;
        }
    }

}
