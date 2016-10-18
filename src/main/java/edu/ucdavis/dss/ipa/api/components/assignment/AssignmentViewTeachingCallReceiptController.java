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
public class AssignmentViewTeachingCallReceiptController {
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
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    
    @RequestMapping(value = "/api/assignmentView/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingCallReceipt updateTeachingCallReceipt(@PathVariable long teachingCallReceiptId, @RequestBody TeachingCallReceipt teachingCallReceipt, HttpServletResponse httpResponse) {
        TeachingCallReceipt originalTeachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Workgroup workgroup = originalTeachingCallReceipt.getTeachingCall().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "senateInstructor", "federationInstructor");

        originalTeachingCallReceipt.setComment(teachingCallReceipt.getComment());
        originalTeachingCallReceipt.setIsDone(teachingCallReceipt.getIsDone());

        return teachingCallReceiptService.save(originalTeachingCallReceipt);
    }

}
