package edu.ucdavis.dss.ipa.api.components.auditLog.views;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class AuditLogExcelView extends AbstractXlsxView {
    private long workgroupId, year;
    private String moduleName;
    private List<AuditLog> auditLogList;

    public AuditLogExcelView(long workgroupId, long year, String moduleName,
                             List<AuditLog> auditLogList) {
        this.workgroupId = workgroupId;
        this.year = year;
        this.moduleName = moduleName;
        this.auditLogList = auditLogList;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook,
                                      HttpServletRequest request, HttpServletResponse response) {
        response.setHeader("Content-Type", "multipart/mixed; charset=\"utf-8\"");
        response.setHeader("Content-Disposition", "attachment; filename=\"Audit-Log.xlsx\"");
        Sheet worksheet = workbook.createSheet("Audit Log");
        worksheet = ExcelHelper.setSheetHeader(worksheet, Arrays.asList("Date", "Message", "Name"));

        for (AuditLog auditLog : auditLogList) {
            List<Object> cellValues = Arrays.asList(auditLog.getCreatedAt(), auditLog.getMessage(), auditLog.getUserName());
            worksheet = ExcelHelper.writeRowToSheet(worksheet, cellValues);
        }

        workbook = ExcelHelper.expandHeaders(workbook);
    }

}
