package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
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
    private final ScheduleSummaryReportView scheduleSummaryReportView;

    public ScheduleSummaryReportAnnualExcelView(ScheduleSummaryReportView scheduleSummaryReportView) {
        this.scheduleSummaryReportView = scheduleSummaryReportView;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        final long year = scheduleSummaryReportView.getYear();
        String fileName = "Annual Schedule";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + fileName + "-" + now.format(formatter) + ".xlsx\"");

        Sheet worksheet = workbook.createSheet(year + "-" + String.valueOf(year + 1).substring(2, 4));

        // Only want Fall, Winter, Spring
        List<String> shortTermCodes =
            Arrays.asList(TermDescription.FALL.getShortTermCode(), TermDescription.WINTER.getShortTermCode(),
                TermDescription.SPRING.getShortTermCode());

        List<String> termHeaders = new ArrayList<>();
        List<Object> sectionHeaders = new ArrayList<>();
        List<List<Object>> dataRows = new ArrayList<>();

        int completedTerm = 0;
        for (String shortTermCode : shortTermCodes) {
            termHeaders.addAll(Arrays.asList(Term.getRegistrarName(shortTermCode), "", ""));
            sectionHeaders.addAll(Arrays.asList("Course", "Instructor", "Cap"));

            List<SectionGroup> termSectionGroups =
                scheduleSummaryReportView.getSectionGroups().stream().filter(sg -> sg.getTermCode().equals(
                    Term.getTermCodeByYearAndShortTermCode(year, shortTermCode))).sorted(
                    Comparator.comparing(sg -> sg.getCourse().getCourseNumber())).collect(Collectors.toList());

            int currentRow = 0;
            for (int i = 0; i < termSectionGroups.size(); i++) {
                SectionGroup currentSectionGroup = termSectionGroups.get(i);
                List<Object> rowValues = new ArrayList<>(Arrays.asList(
                    currentSectionGroup.getCourse().getShortDescription(),
                    currentSectionGroup.getTeachingAssignments().size() > 0 ?
                        currentSectionGroup.getTeachingAssignments().get(0).getInstructorDisplayName() : null,
                    currentSectionGroup.getPlannedSeats()
                ));

                // add filler if previous course number was different
                if (i > 0 && currentSectionGroup.getCourse().getCourseNumber()
                    .compareTo(termSectionGroups.get(i - 1).getCourse().getCourseNumber()) != 0) {

                    int spaces = rowValues.size();

                    if (completedTerm > 0) {
                        List<Object> row = dataRows.get(currentRow);
                        fillRow(row, "", spaces);
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
                    } else {
                        // prepend row with fill spaces
                        int spaces = completedTerm * rowValues.size();
                        fillRow(rowValues, "", spaces, 0);

                        dataRows.add(new ArrayList<>(rowValues));
                    }
                } else {
                    dataRows.add(new ArrayList<>(rowValues));
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
