package edu.ucdavis.dss.ipa.api.components.summary;

import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;
import edu.ucdavis.dss.ipa.api.components.summary.views.factories.SummaryViewFactory;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@CrossOrigin
public class SummaryViewController {
    @Inject SummaryViewFactory summaryViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SupportStaffService supportStaffService;
    @Inject Authorization authorization;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/summaryView/{workgroupId}/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public SummaryView getInitialSummaryView(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");
        User currentUser = userService.getOneByLoginId(authorization.getLoginId());

        // Determine if user is an instructor
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());
        long instructorId = 0;

        if (instructor != null) {
            instructorId = instructor.getId();
        }

        // Determine if user is support staff
        SupportStaff supportStaff = supportStaffService.findByLoginId(currentUser.getLoginId());
        long supportStaffId = 0;

        if (supportStaff != null) {
            supportStaffId = supportStaff.getId();
        }

        return summaryViewFactory.createSummaryView(workgroupId, year, currentUser.getId(), instructorId, supportStaffId);
    }
}
