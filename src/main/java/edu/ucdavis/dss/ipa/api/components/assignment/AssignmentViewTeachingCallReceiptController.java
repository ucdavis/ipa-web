package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.entities.TeachingCallComment;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.TeachingCallCommentService;
import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
public class AssignmentViewTeachingCallReceiptController {
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject TeachingCallCommentService teachingCallCommentService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/assignmentView/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public TeachingCallReceipt updateTeachingCallReceipt(@PathVariable long teachingCallReceiptId, @RequestBody TeachingCallReceipt teachingCallReceipt) {
        TeachingCallReceipt originalTeachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);
        Workgroup workgroup = originalTeachingCallReceipt.getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "instructor");

        originalTeachingCallReceipt.setIsDone(teachingCallReceipt.getIsDone());
        originalTeachingCallReceipt.setUpdatedAt(new Date());

        return teachingCallReceiptService.save(originalTeachingCallReceipt);
    }

    @RequestMapping(value = "/api/assignmentView/teachingCallReceipts/{teachingCallReceiptId}/teachingCallComments", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public TeachingCallComment createTeachingCallComment(@PathVariable long teachingCallReceiptId,
                                                         @RequestBody TeachingCallComment teachingCallCommentDTO,
                                                         HttpServletResponse httpResponse) {
        // Ensure valid params
        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneById(teachingCallReceiptId);

        if (teachingCallReceipt == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        if (teachingCallReceipt.getLocked()) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        // Authorization check
        Long workGroupId = teachingCallReceipt.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workGroupId, "academicPlanner", "instructor");

        teachingCallCommentDTO.setTeachingCallReceipt(teachingCallReceipt);

        TeachingCallComment teachingCallComment = teachingCallCommentService.create(teachingCallCommentDTO);

        if (teachingCallComment == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        teachingCallReceipt.setUpdatedAt(new Date());
        teachingCallReceiptService.save(teachingCallReceipt);

        return teachingCallComment;
    }
}
