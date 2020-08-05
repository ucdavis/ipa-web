package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport;

import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.factories.SupportCallResponseReportViewFactory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.method.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SupportCallResponseReportController {
    @Inject SupportCallResponseReportViewFactory supportCallResponseReportViewFactory;
    @Inject Authorizer authorizer;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

//    @RequestMapping(value = "/api/supportCallResponseReportView/workgroup/{workgroupId}/year/{year}/termCode/{termShortCode}", method = RequestMethod.GET, produces = "application/json")
//    @ResponseBody
//    public View

    @RequestMapping(value = "/api/supportCallResponseReportView/workgroups/{workgroupId}/years/{year}/termCode/{termShortCode}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termShortCode, HttpServletRequest httpRequest) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url = ipaUrlApi + "/download/supportCallResponseReportView/workgroups/" + workgroupId + "/years/" + year + "/termCode/" + termShortCode + "/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    /**
     * Exports the response report as an Excel .xls file
     *
     * @param workgroupId
     * @param year
     * @param termShortCode
     * @param salt
     * @param encrypted
     * @param httpRequest
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/download/supportCallResponseReportView/workgroups/{workgroupId}/years/{year}/termCode/{termShortCode}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termShortCode,
                              @PathVariable String salt, @PathVariable String encrypted,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return supportCallResponseReportViewFactory.createSupportCallResponseReportExcelView(workgroupId, year, termShortCode);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }
}