package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views;

import edu.ucdavis.dss.ipa.entities.SupportStaff;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxView;

public class SupportCallResponseReportExcelView extends AbstractXlsxView {
    private SupportCallResponseReportView supportCallResponseReportViewDTO = null;

    public SupportCallResponseReportExcelView(SupportCallResponseReportView supportCallResponseReportViewDTO) {
        this.supportCallResponseReportViewDTO = supportCallResponseReportViewDTO;
    }

    @Override
    protected void buildExcelDocument(Map<String, Object> model, Workbook workbook, HttpServletRequest request, HttpServletResponse response) {
        String filename = "attachment; filename=\"" + supportCallResponseReportViewDTO.getSchedule().getWorkgroup().getName() + " - " + supportCallResponseReportViewDTO.getSchedule().getYear() + " - SupportCallResponseReport.xlsx\"";

        // Set filename
        response.setHeader("Content-Type", "multipart/mixed; charset=\"UTF-8\"");
        response.setHeader("Content-Disposition", filename);

        Sheet sheet = workbook.createSheet("Support Call Preferences");

        List<SupportStaff> students = supportCallResponseReportViewDTO.getSupportStaff();
    }
}
