package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories.TeachingCallResponseReportViewFactory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@CrossOrigin
public class TeachingCallResponseReportController {
    @Inject TeachingCallResponseReportViewFactory teachingCallResponseReportViewFactory;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/teachingCallResponseReportView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public TeachingCallResponseReportView getTeachingCallResponseReportView(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        return teachingCallResponseReportViewFactory.createTeachingCallResponseReportView(workgroupId, year);
    }
}
