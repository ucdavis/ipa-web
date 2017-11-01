package edu.ucdavis.dss.ipa.api.components.teachingCall;

import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallFormView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class TeachingCallFormViewController {

    @Inject TeachingCallViewFactory teachingCallViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/teachingCallView/{workgroupId}/{year}/teachingCallForm", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public TeachingCallFormView getTeachingCallFormView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "lecturer");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());
        long instructorId = 0;
        // Academic coordinators will not have instructors associated to their user
        if (instructor != null) {
            instructorId = instructor.getId();
        }

        return teachingCallViewFactory.createTeachingCallFormView(workgroupId, year, currentUser.getId(), instructorId);
    }

}
