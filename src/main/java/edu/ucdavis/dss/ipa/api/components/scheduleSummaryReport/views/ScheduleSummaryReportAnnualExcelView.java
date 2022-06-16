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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        String fileName = "Annual Schedule";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + fileName + "-" + now.format(formatter) + ".xlsx\"");

        Sheet worksheet = workbook.createSheet(year + "-" + String.valueOf(year + 1).substring(2, 4));

        List<String> termHeaders = new ArrayList<>();
        List<Object> sectionHeaders = new ArrayList<>();
        List<List<Object>> dataRows = new ArrayList<>();

        int completedTerm = 0;
        for (ScheduleSummaryReportView scheduleSummaryReportView : scheduleSummaryReportViewList) {
            String shortTermCode = scheduleSummaryReportView.getTermCode();
            termHeaders.addAll(Arrays.asList(Term.getRegistrarName(shortTermCode), "", "", "", "", ""));
            sectionHeaders.addAll(Arrays.asList("Course", "Instructor", "Days", "Hours", "Cap", "Prior"));

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

                String lastOfferedTermCode = null;
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

                // add filler if previous course number was different
                if (i > 0 && currentSectionGroup.getCourse().getCourseNumber()
                    .compareTo(termSectionGroups.get(i - 1).getCourse().getCourseNumber()) != 0) {

                    int spaces = rowValues.size();

                    if (completedTerm > 0) {
                        List<Object> row;
                        if (currentRow < dataRows.size()) {
                            row = dataRows.get(currentRow);
                            fillRow(row, "", spaces);
                        } else {
                            row = new ArrayList<>();
                            fillRow(row, "", spaces * completedTerm);
                            dataRows.add(row);
                        }
                        currentRow++;
                    } else {
                        List<Object> emptyRow = new ArrayList<>();
                        fillRow(emptyRow, "", spaces);
                        dataRows.add(new ArrayList<>(emptyRow));
                    }
                }

                if (completedTerm > 0) {
                    // start another column
                    if (currentRow < dataRows.size()) {
                        // append to existing row
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
                                        section.getSeats()
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
                                    section.getSeats()
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
                                        section.getSeats()
                                    ));
                                fillRow(values, "", spaces, 0);
                                dataRows.add(values);
                                currentRow++;
                            }
                        }
                    }
                } else {
                    dataRows.add(rowValues);

                    if (currentSectionGroup.getSections().size() > 1) {
                        for (Section section : currentSectionGroup.getSections()) {
                            dataRows.add(new ArrayList<>(
                                Arrays.asList(
                                    section.getSequenceNumber(),
                                    section.getSupportAssignments().size() > 0 ?
                                        section.getSupportAssignments().get(0).getSupportStaff().getLastName() : "",
                                    "",
                                    "",
                                    section.getSeats()
                                )
                            ));
                        }
                    }
                }


//                // add blank row if next course number is different
//                if (i + 1 < termSectionGroups.size() && sectionGroup.getCourse().getCourseNumber()
//                    .compareTo(termSectionGroups.get(i + 1).getCourse().getCourseNumber()) != 0) {
//                    if (completedTerm > 0) {
//                        // append to existing row
//                        int fillSpaces = rowValues.size();
//                        List<Object> nextRow = dataRows.get(currentRow + 1);
//                        for (int i = 0; i < fillSpaces; i++) {
//                            nextRow.add("");
//                        }
//                        currentRow++;
//                    } else {
//                        int fillSpaces = rowValues.size();
//                        List<Object> emptyRow = new ArrayList<>();
//                        for (int i = 0; i < fillSpaces; i++) {
//                            emptyRow.add("");
//                        }
//                        dataRows.add(new ArrayList<>(emptyRow));
//                    }
//                }
                currentRow++;
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

    private List<Object> fillRow(List<Object> row, Object value, int length) {
        for (int i = 0; i < length; i++) {
            row.add(value);
        }
        return row;
    }

    private List<Object> fillRow(List<Object> row, Object value, int length, int start) {
        for (int i = 0; i < length; i++) {
            row.add(start, value);
        }
        return row;
    }
}
