package edu.ucdavis.dss.ipa.api.components.summary;

import edu.ucdavis.dss.ipa.api.components.summary.views.SummaryView;
import edu.ucdavis.dss.ipa.api.components.summary.views.factories.SummaryViewFactory;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class SummaryViewController {

    @Inject SummaryViewFactory summaryViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject WorkgroupService workgroupService;
    @Inject
    SupportStaffService supportStaffService;
    @RequestMapping(value = "/api/summaryView/{workgroupId}/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public SummaryView getInitialSummaryView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "studentPhd", "studentMasters", "instructionalSupport");
        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        // TODO: Determine if user is an academic coordinator, and get notices/events/etc data


        // Determine if user is an instructor
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());
        long instructorId = 0;
        // Academic coordinators will not have instructors associated to their user
        if (instructor != null) {
            instructorId = instructor.getId();
        }

        // Determine if user is an instructional support staff
        long supportStaffId = 0;
        SupportStaff supportStaff = supportStaffService.findByLoginId(currentUser.getLoginId());
        if (supportStaff != null) {
            supportStaffId = supportStaff.getId();
        }

        return summaryViewFactory.createSummaryView(workgroupId, year, currentUser.getId(), instructorId, supportStaffId);
    }
}
