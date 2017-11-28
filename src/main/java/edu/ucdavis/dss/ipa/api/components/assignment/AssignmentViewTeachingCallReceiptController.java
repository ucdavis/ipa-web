package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
@CrossOrigin
public class AssignmentViewTeachingCallReceiptController {
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject Authorizer authorizer;
    
    @RequestMapping(value = "/api/assignmentView/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingCallReceipt updateTeachingCallReceipt(@PathVariable long teachingCallReceiptId, @RequestBody TeachingCallReceipt teachingCallReceipt) {
        TeachingCallReceipt originalTeachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Workgroup workgroup = originalTeachingCallReceipt.getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "senateInstructor", "federationInstructor", "lecturer");

        originalTeachingCallReceipt.setComment(teachingCallReceipt.getComment());
        originalTeachingCallReceipt.setIsDone(teachingCallReceipt.getIsDone());

        return teachingCallReceiptService.save(originalTeachingCallReceipt);
    }

}
