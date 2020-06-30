package edu.ucdavis.dss.ipa.api.components.budget.views;

import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.Budget;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.services.BudgetService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BudgetExcelView extends AbstractXlsView {
    @Inject BudgetViewFactory budgetViewFactory;
    @Inject BudgetService budgetService;
    @Inject BudgetScenarioService budgetScenarioService;
    private List<BudgetView> budgetViews = null;
    public BudgetExcelView ( List<BudgetView> budgetViews ) {
        this.budgetViews = budgetViews;
    }

    /*
        Make sure to edit the frontend if any calculations change!!!
     */
    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Budget-Report.xls\"");

        Sheet sheet = workbook.createSheet("Budget Summary");
        setExcelHeader(sheet, Arrays.asList("1", "2"));
        for(BudgetView budgetView : budgetViews){
            System.err.println("Instructor count in excel is " + budgetView.getActiveInstructors().size());
            Row excelData = sheet.createRow(1);
            excelData.createCell(0).setCellValue(budgetView.getActiveInstructors().size());
        }
    }

    private void setExcelHeader(Sheet sheet, List<String> headers) {
        Row excelHeader = sheet.createRow(0);
        for(int i = 0; i < headers.size(); i++){
            excelHeader.createCell(i).setCellValue(headers.get(i));
        }
    }
}
