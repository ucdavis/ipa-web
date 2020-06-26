package edu.ucdavis.dss.ipa.api.components.budget.views;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BudgetExcelView extends AbstractXlsView {
    //private BudgetView budgetViewDTO = null;
    /*public BudgetExcelView (BudgetView budgetViewDTO) {
        this.budgetViewDTO = budgetViewDTO;
    }*/
    public BudgetExcelView (){

    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Budget-Report.xls\"");

        Sheet sheet = workbook.createSheet("Budget Summary");
        setExcelHeader(sheet, Arrays.asList("1", "2"));

    }

    private void setExcelHeader(Sheet sheet, List<String> headers) {
        Row excelHeader = sheet.createRow(0);
        for(int i = 0; i < headers.size(); i++){
            excelHeader.createCell(i).setCellValue(headers.get(i));
        }
    }
}
