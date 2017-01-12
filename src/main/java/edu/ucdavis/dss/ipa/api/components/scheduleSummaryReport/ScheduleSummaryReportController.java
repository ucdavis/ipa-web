package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories.ScheduleSummaryViewFactory;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class ScheduleSummaryReportController {

    @Inject ScheduleSummaryViewFactory scheduleSummaryViewFactory;

    @RequestMapping(value = "/api/scheduleSummaryReportView/workgroups/{workgroupId}/years/{year}/terms/{termCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ScheduleSummaryReportView getScheduleSummaryView(@PathVariable long workgroupId, @PathVariable long year,
                                                        @PathVariable String termCode, HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return scheduleSummaryViewFactory.createScheduleSummaryReportView(workgroupId, year, termCode);
    }

    @RequestMapping(value = "/api/scheduleSummaryReportView/workgroups/{workgroupId}/years/{year}/terms/{termCode}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termCode,
                                             HttpServletRequest httpRequest) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url = SettingsConfiguration.getIpaApiURL() + "/download/scheduleSummaryReportView/workgroups/" + workgroupId + "/years/"+ year + "/terms/" + termCode + "/excel";
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
     * Exports a schedule as an Excel .xls file
     *
     * @param workgroupId
     * @param year
     * @param salt
     * @param encrypted
     * @param httpRequest
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/download/scheduleSummaryReportView/workgroups/{workgroupId}/years/{year}/terms/{termCode}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termCode,
                              @PathVariable String salt, @PathVariable String encrypted,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return scheduleSummaryViewFactory.createScheduleSummaryReportExcelView(workgroupId, year, termCode);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }

}
