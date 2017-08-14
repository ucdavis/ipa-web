package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories.TeachingCallResponseReportViewFactory;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

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
