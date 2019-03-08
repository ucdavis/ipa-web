package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.entities.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BudgetExcelView extends AbstractXlsView {
    private BudgetView budgetViewDTO;

    public BudgetExcelView(BudgetView budgetView) {
        this.budgetViewDTO = budgetView;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        // TODO: Mauricio makes Excel download here. See ScheduleSummaryReportExcelView.java

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        String header = "attachment; filename=BudgetSummary-" + ".xls";
        response.setHeader("Content-Disposition", header);

        // Create sheets
        Sheet byCourseSheet = workbook.createSheet("By Course");
        Sheet byInstructorSheet = workbook.createSheet("By Instructor");

        setExcelHeader(byCourseSheet);
        setExcelHeader(byInstructorSheet);

        String workgroupId= budgetViewDTO.budgetScenarios.get(0).getName();


        int row = 1;
        Row excelHeader = byCourseSheet.createRow(row);
        excelHeader.createCell(0).setCellValue("TEST");
        excelHeader.createCell(1).setCellValue(workgroupId);



    }

    private void setExcelHeader(Sheet excelSheet) {
        Row excelHeader = excelSheet.createRow(0);

        excelHeader.createCell(0).setCellValue("Course");
        excelHeader.createCell(1).setCellValue("Instructors");
        excelHeader.createCell(2).setCellValue("TAs");
        excelHeader.createCell(3).setCellValue("Section");
        excelHeader.createCell(4).setCellValue("CRN");
        excelHeader.createCell(5).setCellValue("Seats");
        excelHeader.createCell(6).setCellValue("Section TAs");
        excelHeader.createCell(7).setCellValue("Activity");
        excelHeader.createCell(8).setCellValue("Days");
        excelHeader.createCell(9).setCellValue("Start");
        excelHeader.createCell(10).setCellValue("End");
        excelHeader.createCell(11).setCellValue("Location");
    }
}
