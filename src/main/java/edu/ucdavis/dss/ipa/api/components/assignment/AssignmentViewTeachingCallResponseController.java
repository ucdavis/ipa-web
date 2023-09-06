package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import java.util.Date;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Lloyd on 8/10/16.
 */
@RestController
public class AssignmentViewTeachingCallResponseController {
    @Inject ScheduleService scheduleService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject TeachingCallResponseService teachingCallResponseService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/assignmentView/teachingCallResponses/{teachingCallResponseId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public TeachingCallResponse updateTeachingCallResponse(@PathVariable long teachingCallResponseId, @RequestBody TeachingCallResponse teachingCallResponse, HttpServletResponse httpResponse) {
        TeachingCallResponse originalTeachingCallResponse = teachingCallResponseService.getOneById(teachingCallResponseId);
        Workgroup workgroup = originalTeachingCallResponse.getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "instructor");

        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneByScheduleIdAndInstructorId(originalTeachingCallResponse.getSchedule().getId(), originalTeachingCallResponse.getInstructor().getId());

        if (teachingCallReceipt.getLocked()) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        teachingCallReceipt.setUpdatedAt(new Date());
        teachingCallReceiptService.save(teachingCallReceipt);

        originalTeachingCallResponse.setAvailabilityBlob(teachingCallResponse.getAvailabilityBlob());

        return teachingCallResponseService.save(originalTeachingCallResponse);
    }


    /**
     * Creates a new teachingCallResponse.
     * @param scheduleId
     * @param instructorId
     * @param teachingCallResponseDTO
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/assignmentView/teachingCallResponses/{scheduleId}/{instructorId}", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public TeachingCallResponse createTeachingCallResponse(@PathVariable long scheduleId, @PathVariable long instructorId, @RequestBody TeachingCallResponse teachingCallResponseDTO, HttpServletResponse httpResponse) {
        // Verify params
        Schedule schedule = scheduleService.findById(scheduleId);
        Instructor instructor = instructorService.getOneById(instructorId);

        if (schedule == null || instructor == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        // Authorize user
        Workgroup workgroup = schedule.getWorkgroup();
        authorizer.hasWorkgroupRoles(workgroup.getId(), "instructor");

        TeachingCallResponse teachingCallResponse = teachingCallResponseService.findOrCreateOneByScheduleIdAndInstructorIdAndTermCode(schedule.getId(), instructor.getId(), teachingCallResponseDTO.getTermCode());

        teachingCallResponse.setAvailabilityBlob(teachingCallResponseDTO.getAvailabilityBlob());

        TeachingCallReceipt teachingCallReceipt = teachingCallReceiptService.findOneByScheduleIdAndInstructorId(schedule.getId(), instructor.getId());
        teachingCallReceipt.setUpdatedAt(new Date());
        teachingCallReceiptService.save(teachingCallReceipt);

        return teachingCallResponseService.save(teachingCallResponse);
    }
}
