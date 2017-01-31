package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories.ScheduleSummaryViewFactory;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories.TeachingCallResponseReportViewFactory;
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
public class TeachingCallResponseReportController {

    @Inject
    TeachingCallResponseReportViewFactory teachingCallResponseReportViewFactory;

    @RequestMapping(value = "/api/teachingCallResponseReportView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TeachingCallResponseReportView getTeachingCallResponseReportView(@PathVariable long workgroupId, @PathVariable long year,
                                                                 HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return teachingCallResponseReportViewFactory.createTeachingCallResponseReportView(workgroupId, year);
    }
}
