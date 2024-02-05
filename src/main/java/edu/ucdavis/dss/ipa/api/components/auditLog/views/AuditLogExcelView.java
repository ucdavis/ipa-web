package edu.ucdavis.dss.ipa.api.components.auditLog.views;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.utilities.ExcelHelper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        worksheet = ExcelHelper
            .setSheetHeader(worksheet, Arrays.asList("Date", "Section Details", "Action", "Name"));

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");

        for (AuditLog entry : auditLogList) {
            LocalDateTime createdDateTime =
                entry.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            String[] message = entry.getMessage().replace("*", "").split("\\r?\\n");
            String sectionDetails = message[0];
            String action = message[1];

            String formattedSectionDetails =
                sectionDetails.substring(sectionDetails.indexOf(" - ") + " - ".length());

            List<Object> cellValues = Arrays
                .asList(dateFormatter.format(createdDateTime), formattedSectionDetails, action,
                    entry.getUserName());
            worksheet = ExcelHelper.writeRowToSheet(worksheet, cellValues);
        }

        workbook = ExcelHelper.expandHeaders(workbook);
    }
}
