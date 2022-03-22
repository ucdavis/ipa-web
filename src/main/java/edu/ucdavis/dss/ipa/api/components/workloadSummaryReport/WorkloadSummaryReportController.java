package edu.ucdavis.dss.ipa.api.components.workloadSummaryReport;

import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.WorkloadSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.workloadSummaryReport.views.factories.WorkloadSummaryReportViewFactory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;

@RestController
public class WorkloadSummaryReportController {
    @Inject
    WorkloadSummaryReportViewFactory workloadSummaryReportViewFactory;
    @Inject
    Authorizer authorizer;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public WorkloadSummaryReportView getWorkloadSummaryReportView(
        @PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return workloadSummaryReportViewFactory.createWorkloadSummaryReportView(workgroupId, year);
    }

    @RequestMapping(value = "/api/workloadSummaryReport/{workgroupId}/years/{year}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long[] workgroupId,
                                             @PathVariable long year,
                                             HttpServletRequest httpRequest) {
//        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url =
            ipaUrlApi + "/download/workloadSummaryReport/" + workgroupId +
                "/years/" + year + "/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    @RequestMapping(value = "/download/workloadSummaryReport/{workgroupId}/years/{year}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long[] workgroupId, @PathVariable long year,
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
                .createWorkloadSummaryReportExcelView(workgroupId, year);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }
}
