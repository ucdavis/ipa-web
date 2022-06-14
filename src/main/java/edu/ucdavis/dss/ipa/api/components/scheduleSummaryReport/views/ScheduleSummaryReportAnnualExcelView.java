package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
        String dateOfDownload = new Date().toString();
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
            for (SectionGroup sectionGroup : termSectionGroups) {
                List<Object> rowValues = new ArrayList<>(Arrays.asList(
                    sectionGroup.getCourse().getShortDescription(),
                    sectionGroup.getTeachingAssignments().size() > 0 ?
                        sectionGroup.getTeachingAssignments().get(0).getInstructorDisplayName() : null,
                    sectionGroup.getPlannedSeats()
                ));

                if (completedTerm > 0) {
                    // start another column
                    if (currentRow < dataRows.size()) {
                        // append to existing row
                        dataRows.get(currentRow).addAll(rowValues);
                    } else {
                        // prepend row with fill spaces
                        int fillSpaces = completedTerm * rowValues.size();
                        for (int i = 0; i < fillSpaces; i++) {
                            rowValues.add(0, "");
                        }

                        dataRows.add(new ArrayList<>(rowValues));
                    }
                } else {
                    dataRows.add(new ArrayList<>(rowValues));
                }
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
}
