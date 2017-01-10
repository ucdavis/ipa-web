package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories.ScheduleSummaryViewFactory;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class ScheduleSummaryReportController {

    @Inject ScheduleSummaryViewFactory scheduleSummaryViewFactory;

    @RequestMapping(value = "/api/scheduleSummaryReportView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ScheduleSummaryReportView getScheduleSummaryView(@PathVariable long workgroupId, @PathVariable long year,
                                                        @PathVariable String termCode, HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return scheduleSummaryViewFactory.createScheduleSummaryReportView(workgroupId, year, termCode);
    }
}
