package edu.ucdavis.dss.ipa.api.components.assignment;

        import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
        import edu.ucdavis.dss.ipa.services.*;
        import org.springframework.http.HttpStatus;
        import org.springframework.web.bind.annotation.*;

        import javax.inject.Inject;
        import javax.servlet.http.HttpServletResponse;
        import java.util.ArrayList;
        import java.util.List;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class AssignmentViewTeachingCallResponseController {
    @Inject CurrentUser currentUser;
    @Inject AuthenticationService authenticationService;
    @Inject WorkgroupService workgroupService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallResponseService teachingCallResponseService;
    @Inject TeachingCallService teachingCallService;

    @RequestMapping(value = "/api/assignmentView/teachingCallResponses/{teachingCallResponseId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingCallResponse updateTeachingCallResponse(@PathVariable long teachingCallResponseId, @RequestBody TeachingCallResponse teachingCallResponse, HttpServletResponse httpResponse) {
        TeachingCallResponse originalTeachingCallResponse = teachingCallResponseService.getOneById(teachingCallResponseId);
        Workgroup workgroup = originalTeachingCallResponse.getTeachingCall().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "senateInstructor", "federationInstructor");

        originalTeachingCallResponse.setAvailabilityBlob(teachingCallResponse.getAvailabilityBlob());

        return teachingCallResponseService.save(originalTeachingCallResponse);
    }


    /**
     * Creates a new teachingCallResponse.
     * @param teachingCallId
     * @param instructorId
     * @param teachingCallResponseDTO
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/assignmentView/teachingCallResponses/{teachingCallId}/{instructorId}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingCallResponse createTeachingCallResponse(@PathVariable long teachingCallId, @PathVariable long instructorId, @RequestBody TeachingCallResponse teachingCallResponseDTO, HttpServletResponse httpResponse) {
        // Verify params
        TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
        Instructor instructor = instructorService.getOneById(instructorId);

        if (teachingCall == null || instructor == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorize user
        Workgroup workgroup = teachingCall.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "federationInstructor", "senateInstructor");

        TeachingCallResponse teachingCallResponse = teachingCallResponseService.findOrCreateOneByTeachingCallIdAndInstructorIdAndTermCode(teachingCall.getId(), instructor.getId(), teachingCallResponseDTO.getTermCode());

        teachingCallResponse.setAvailabilityBlob(teachingCallResponseDTO.getAvailabilityBlob());

        return teachingCallResponseService.save(teachingCallResponse);
    }
}
