package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.ArrayList;
import java.util.Arrays;
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
    private ScheduleSummaryReportView scheduleSummaryReportView;

    public ScheduleSummaryReportAnnualExcelView(ScheduleSummaryReportView scheduleSummaryReportView) {
        this.scheduleSummaryReportView = scheduleSummaryReportView;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        final long year = scheduleSummaryReportView.getYear();
        String dateOfDownload = new Date().toString();
        String fileName = "Annual Schedule";

        response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;");
        response.setHeader("Content-Disposition",
            "attachment; filename=\"" + fileName + "-" + dateOfDownload + ".xlsx\"");

        Sheet worksheet = workbook.createSheet(year + "-" + String.valueOf(year + 1).substring(2, 4));

        // Only want Fall, Winter, Spring
        List<String> shortTermCodes =
            Arrays.asList(TermDescription.FALL.getShortTermCode(), TermDescription.WINTER.getShortTermCode(),
                TermDescription.SPRING.getShortTermCode());

        List<String> termHeaders = new ArrayList<>();
        List<Object> sectionHeaders = new ArrayList<>();
        List<List<Object>> dataRows = new ArrayList<>();

        for (String shortTermCode : shortTermCodes) {
            termHeaders.addAll(Arrays.asList(Term.getRegistrarName(shortTermCode), "", "", ""));
            sectionHeaders.addAll(Arrays.asList("Course", "Instructor", "Cap"));

            List<SectionGroup> termSectionGroups =
                scheduleSummaryReportView.getSectionGroups().stream().filter(sg -> sg.getTermCode().equals(
                    Term.getTermCodeByYearAndShortTermCode(year, shortTermCode))).collect(Collectors.toList());

            int completedTerm = 0;
            int currentRow = 0;
            for (SectionGroup sectionGroup : termSectionGroups) {
                List<Object> data = new ArrayList<>(Arrays.asList(
                    sectionGroup.getCourse().getShortDescription(),
                    sectionGroup.getTeachingAssignments().size() > 0 ?
                        sectionGroup.getTeachingAssignments().get(0).getInstructorDisplayName() : null,
                    sectionGroup.getPlannedSeats(),
                    sectionGroup.getTermCode()
                ));

                if (completedTerm > 0) {
                    // start another column
                    if (currentRow < dataRows.size()) {
                        // append to existing row
                        dataRows.get(currentRow).addAll(data);
                    } else {
                        List<Object> dataRow = new ArrayList<>();

                        // create new row with fill spaces
                        int fillSpaces = completedTerm * data.size();
                        for (int i = 0; i < fillSpaces; i++) {
                            data.add(0, "");
                        }

                        dataRow.addAll(data);
                        dataRows.add(dataRow);
                    }
                } else {
                    List<Object> dataRow = new ArrayList<>();
                    dataRow.addAll(data);
                    dataRows.add(dataRow);
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
