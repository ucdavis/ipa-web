package edu.ucdavis.dss.ipa.api.components.assignment;

        import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
        import edu.ucdavis.dss.ipa.services.*;
        import org.springframework.web.bind.annotation.*;

        import javax.inject.Inject;
        import javax.servlet.http.HttpServletResponse;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class AssignmentViewTeachingCallResponseController {
    @Inject
    CurrentUser currentUser;
    @Inject
    AuthenticationService authenticationService;
    @Inject
    WorkgroupService workgroupService;
    @Inject
    ScheduleService scheduleService;
    @Inject
    CourseService courseService;
    @Inject
    TeachingAssignmentService teachingAssignmentService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallResponseService teachingCallResponseService;

    @RequestMapping(value = "/api/assignmentView/teachingCallResponses/{teachingCallResponseId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingCallResponse updateTeachingCallReceipt(@PathVariable long teachingCallResponseId, @RequestBody TeachingCallResponse teachingCallResponse, HttpServletResponse httpResponse) {
        TeachingCallResponse originalTeachingCallResponse = teachingCallResponseService.getOneById(teachingCallResponseId);
        Workgroup workgroup = originalTeachingCallResponse.getTeachingCall().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        originalTeachingCallResponse.setAvailabilityBlob(teachingCallResponse.getAvailabilityBlob());

        return teachingCallResponseService.save(originalTeachingCallResponse);
    }

}
