package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.api.components.auditLog.views.AuditLogExcelView;
import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.AuditLogService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
public class AuditLogController {
    @Inject
    AuditLogService auditLogService;
    @Inject
    WorkgroupService workgroupService;
    @Inject
    Authorizer authorizer;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/modules/{moduleName}/auditLogs", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<AuditLog> getAuditLogs(@PathVariable long workgroupId,
                                       @PathVariable String moduleName,
                                       HttpServletResponse httpResponse) {

        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");

        return auditLogService
            .findByWorkgroupIdAndModuleOrderByCreatedAtDesc(workgroupId, moduleName);
    }

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/modules/{moduleName}/auditLogs", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<AuditLog> getAuditLogsByYear(@PathVariable long workgroupId,
                                             @PathVariable long year,
                                             @PathVariable String moduleName,
                                             HttpServletResponse httpResponse) {

        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");

        return auditLogService
            .findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(workgroupId, year, moduleName);
    }

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/modules/{moduleName}/auditLogs/download", method = RequestMethod.POST)
    @ResponseBody
    public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
                              @PathVariable String moduleName,
                              HttpServletRequest httpRequest) {
        List<AuditLog> auditLogs = auditLogService
            .findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(workgroupId, year, moduleName);

        return new AuditLogExcelView(workgroupId, year, moduleName, auditLogs);
    }
}
