package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.factories.AssignmentViewFactory;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class AssignmentViewController {

    @Inject
    AssignmentViewFactory assignmentViewFactory;

    @RequestMapping(value = "/api/assignmentView/{workgroupId}/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public AssignmentView getWorkgroupViewByCode(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return assignmentViewFactory.createAssignmentView(workgroupId, year);
    }
}
