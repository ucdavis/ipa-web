package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories.WorkloadSummaryReportViewFactory;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.services.WorkloadSnapshotService;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
public class WorkloadSnapshotController {
    @Inject
    UserService userService;

    @Inject
    WorkgroupService workgroupService;

    @Inject
    Authorizer authorizer;

    @Inject
    Authorization authorization;

    @Inject
    WorkloadSnapshotService workloadSnapshotService;

    @Inject
    WorkloadSummaryReportViewFactory workloadSummaryReportViewFactory;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @Value("${IPA_URL_FRONTEND}")
    String ipaUrlFrontend;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/workloadSnapshots", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<WorkloadSnapshot> getWorkloadSnapshots(@PathVariable long workgroupId, @PathVariable long year,
                                                       HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
        List<WorkloadSnapshot> workloadSnapshots = workloadSnapshotService.findByWorkgroupIdAndYear(workgroupId, year);

        return workloadSnapshots;
    }

    @RequestMapping(value = "/api/workloadSnapshots/{workloadSnapshotId}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long workloadSnapshotId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        WorkloadSnapshot snapshot = workloadSnapshotService.findById(workloadSnapshotId);

        if (snapshot == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        long workgroupId = snapshot.getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url =
            ipaUrlApi + "/download/workloadSnapshots/" + workloadSnapshotId + "/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    @RequestMapping(value = "/download/workloadSnapshots/{workloadSnapshotId}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long workloadSnapshotId,
                              @PathVariable String salt, @PathVariable String encrypted,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse)
        throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return workloadSummaryReportViewFactory
                .createWorkloadSummaryReportExcelView(workloadSnapshotId);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }
}
