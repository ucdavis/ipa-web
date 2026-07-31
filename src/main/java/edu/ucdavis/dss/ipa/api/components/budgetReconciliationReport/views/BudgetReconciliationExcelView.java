package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

/**
 * Serves the reconciliation workbook as a download. All sheet building lives in
 * BudgetReconciliationWorkbook, which has no servlet dependency — see the note there.
 */
public class BudgetReconciliationExcelView extends AbstractXlsxView {
    private final BudgetReconciliationWorkbook builder;

    public BudgetReconciliationExcelView(BudgetReconciliationReportView reportView) {
        this.builder = new BudgetReconciliationWorkbook(reportView);
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Disposition",
            String.format("attachment; filename=\"%s\"", builder.fileName()));
        builder.writeInto(workbook);
    }
}
