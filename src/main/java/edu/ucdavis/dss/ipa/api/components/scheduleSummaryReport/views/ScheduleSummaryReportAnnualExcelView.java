package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class ScheduleSummaryReportAnnualExcelView extends AbstractXlsxView {
    private final List<ScheduleSummaryReportView> scheduleSummaryReportViewList;

    public ScheduleSummaryReportAnnualExcelView(List<ScheduleSummaryReportView> scheduleSummaryReportViewList) {
        this.scheduleSummaryReportViewList = scheduleSummaryReportViewList;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        final long year = scheduleSummaryReportViewList.get(0).getYear();
        String fileName = scheduleSummaryReportViewList.get(0).getWorkgroup().getCode() + " Annual Schedule";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + fileName + "-" + now.format(formatter) + ".xlsx\"");

        Sheet worksheet = workbook.createSheet(year + "-" + String.valueOf(year + 1).substring(2, 4));

        List<String> columnNames = Arrays.asList("Course", "Instructor", "Days", "Hours", "Cap", "Prior");
        List<String> termHeaders = new ArrayList<>();
        List<Object> sectionHeaders = new ArrayList<>();
        List<List<Object>> dataRows = new ArrayList<>();

        int completedTerm = 0;
        for (ScheduleSummaryReportView scheduleSummaryReportView : scheduleSummaryReportViewList) {
            String shortTermCode = scheduleSummaryReportView.getTermCode();
            termHeaders.addAll(Arrays.asList(Term.getRegistrarName(shortTermCode), "", "", "", "", ""));
            sectionHeaders.addAll(columnNames);

            List<SectionGroup> termSectionGroups = scheduleSummaryReportView.getSectionGroups().stream()
                .sorted(Comparator.comparing(sg -> sg.getCourse().getCourseNumber())).collect(
                    Collectors.toList());

            int currentRow = 0;
            for (int i = 0; i < termSectionGroups.size(); i++) {
                SectionGroup currentSectionGroup = termSectionGroups.get(i);

                Activity lectureActivity = null;
                if (currentSectionGroup.getActivities().size() == 1) {
                    lectureActivity = currentSectionGroup.getActivities().get(0);
                } else if (currentSectionGroup.getActivities().size() > 0) {
                    lectureActivity =
                        currentSectionGroup.getActivities().stream().filter(Activity::isLecture).findFirst()
                            .orElse(null);
                } else if (currentSectionGroup.getSections().size() > 0) {
                    lectureActivity = currentSectionGroup.getSections().get(0).getActivities().stream().filter(
                        Activity::isLecture).findFirst().orElse(null);
                }

                String days = lectureActivity != null ?
                    lectureActivity.getDayIndicatorDescription() : "";
                String hours = lectureActivity != null ?
                    lectureActivity.getTimeDescription() : "";

                // prior enrollment?
                Map<String, Map<String, Long>> courseCensusMap = scheduleSummaryReportView.getCourseCensus();

                List<String> offeredTermCodes = courseCensusMap.keySet().stream().sorted(
                    Comparator.reverseOrder()).collect(
                    Collectors.toList());

                Course course = currentSectionGroup.getCourse();

                String lastOfferedCensus = null;
                String lastOfferedCourseKey =
                    course.getSubjectCode() + "-" + course.getCourseNumber() + "-" +
                        course.getSequencePattern();

                // walk backwards to find last offering
                for (String termCode : offeredTermCodes) {
                    Map<String, Long> censusMap = courseCensusMap.get(termCode);

                    if (censusMap.containsKey(lastOfferedCourseKey)) {
                        lastOfferedCensus = censusMap.get(lastOfferedCourseKey) +
                            " (" +
                            Term.getShortDescription(termCode) + ")";
                        break;
                    }
                }

                List<Object> rowValues = new ArrayList<>(Arrays.asList(
                    currentSectionGroup.getCourse().getShortDescription(),
                    currentSectionGroup.getTeachingAssignments().size() > 0 ?
                        currentSectionGroup.getTeachingAssignments().get(0).getInstructorDisplayName() : null,
                    days,
                    hours,
                    currentSectionGroup.getPlannedSeats(),
                    lastOfferedCensus
                ));

                // add spaces between courses
                if (i > 0 && currentSectionGroup.getCourse().getShortDescription()
                    .compareTo(termSectionGroups.get(i - 1).getCourse().getShortDescription()) != 0) {

                    if (currentRow == dataRows.size()) {
                        List<Object> emptyRow = new ArrayList<>();
                        fillRow(emptyRow, "", columnNames.size() * (completedTerm + 1));
                        dataRows.add(new ArrayList<>(emptyRow));
                        currentRow += 1;
                    } else {
                        if (currentRow + 1 >= dataRows.size()) {
                            List<Object> emptyRow = new ArrayList<>();
                            fillRow(emptyRow, "", columnNames.size() * (completedTerm + 1));
                            dataRows.add(new ArrayList<>(emptyRow));
                        } else {
                            List<Object> row = dataRows.get(currentRow + 1);
                            fillRow(row, "", columnNames.size());
                        }
                        currentRow += 2;
                    }
                }

                if (completedTerm > 0) {
                    // start another column
                    if (currentRow < dataRows.size()) {
                        // append to existing row
                        int expectedColumns = completedTerm * columnNames.size();
                        int currentColumns = dataRows.get(currentRow).size();
                        if (dataRows.get(currentRow).size() < expectedColumns) {
                            fillRow(dataRows.get(currentRow), "", expectedColumns - currentColumns);
                        }
                        dataRows.get(currentRow).addAll(rowValues);

                        if (currentSectionGroup.getSections().size() > 1 &&
                            (currentRow + currentSectionGroup.getSections().size() < dataRows.size())) {
                            int sectionsAdded = 0;
                            for (Section section : currentSectionGroup.getSections()) {
                                dataRows.get(currentRow + 1 + sectionsAdded).addAll(
                                    Arrays.asList(
                                        section.getSequenceNumber(),
                                        section.getSupportAssignments().size() > 0 ?
                                            section.getSupportAssignments().get(0).getSupportStaff().getLastName() :
                                            "",
                                        "",
                                        "",
                                        section.getSeats(),
                                        ""
                                    )
                                );
                                sectionsAdded++;
                            }
                            currentRow += sectionsAdded;
                        } else if (currentSectionGroup.getSections().size() > 1 &&
                            (currentRow + currentSectionGroup.getSections().size() > dataRows.size())) {
                            int sectionsAdded = 0;
                            for (Section section : currentSectionGroup.getSections()) {
                                int spaces = completedTerm * rowValues.size();

                                List<Object> values = new ArrayList<>(Arrays.asList(
                                    section.getSequenceNumber(),
                                    section.getSupportAssignments().size() > 0 ?
                                        section.getSupportAssignments().get(0).getSupportStaff().getLastName() : "",
                                    "",
                                    "",
                                    section.getSeats(),
                                    ""
                                ));
                                fillRow(values, "", spaces, 0);
                                dataRows.add(values);

                                sectionsAdded++;
                            }
                            currentRow += sectionsAdded;
                        }
                    } else {
                        // prepend new row with fill spaces
                        int spaces = completedTerm * rowValues.size();
                        fillRow(rowValues, "", spaces, 0);
                        dataRows.add(rowValues);
                        currentRow++;

                        // can be removed?
                        if (currentSectionGroup.getSections().size() > 1) {
                            for (Section section : currentSectionGroup.getSections()) {
                                List<Object> values = new ArrayList<>(
                                    Arrays.asList(
                                        currentSectionGroup.getCourseIdentification() + " " +
                                            section.getSequenceNumber(),
                                        section.getSupportAssignments().size() > 0 ?
                                            section.getSupportAssignments().get(0).getSupportStaff().getLastName() :
                                            "",
                                        section.getSeats(),
                                        ""
                                    ));
                                fillRow(values, "", spaces, 0);
                                dataRows.add(values);
                                currentRow++;
                            }
                        }
                    }
                } else {
                    dataRows.add(rowValues);
                    currentRow++;

                    if (currentSectionGroup.getSections().size() > 1) {
                        for (Section section : currentSectionGroup.getSections()) {
                            dataRows.add(new ArrayList<>(
                                Arrays.asList(
                                    section.getSequenceNumber(),
                                    section.getSupportAssignments().size() > 0 ?
                                        section.getSupportAssignments().get(0).getSupportStaff().getLastName() : "",
                                    "",
                                    "",
                                    section.getSeats(),
                                    ""
                                )
                            ));
                            currentRow++;
                        }
                    }
                }
            }
            completedTerm++;
        }

        // write data
        ExcelHelper.setSheetHeader(worksheet, termHeaders);
        ExcelHelper.writeRowToSheet(worksheet, sectionHeaders);
        for (List<Object> dataRow : dataRows) {
            ExcelHelper.writeRowToSheet(worksheet, dataRow);
        }

        ExcelHelper.expandHeaders(workbook);
    }

    private void fillRow(List<Object> row, Object value, int length) {
        for (int i = 0; i < length; i++) {
            row.add(value);
        }
    }

    private void fillRow(List<Object> row, Object value, int length, int start) {
        for (int i = 0; i < length; i++) {
            row.add(start, value);
        }
    }
}
