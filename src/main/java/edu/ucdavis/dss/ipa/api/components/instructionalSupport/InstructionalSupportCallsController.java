package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class InstructionalSupportCallsController {

    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;
    @Inject InstructorInstructionalSupportCallService instructorInstructionalSupportCallService;
    @Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/supportCallStatus", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallStatusView getInstructionalSupportCallView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        return instructionalSupportViewFactory.createSupportCallStatusView(workgroupId, year);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/studentInstructionalSupportCalls", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public StudentSupportCall addStudentSupportCall(@PathVariable long scheduleId, @RequestBody StudentSupportCall studentSupportCall, HttpServletResponse httpResponse) {


        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        studentSupportCall.setSchedule(schedule);

        return studentInstructionalSupportCallService.findOrCreate(studentSupportCall);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/instructorInstructionalSupportCalls", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public InstructorSupportCall addInstructorSupportCall(@PathVariable long scheduleId, @RequestBody InstructorSupportCall instructorSupportCall, HttpServletResponse httpResponse) {
        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        instructorSupportCall.setSchedule(schedule);

        return instructorInstructionalSupportCallService.findOrCreate(instructorSupportCall);
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructorInstructionalSupportCalls/{instructorInstructionalSupportCallId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteInstructorSupportCall(@PathVariable long instructorInstructionalSupportCallId, HttpServletResponse httpResponse) {
        InstructorSupportCall instructorSupportCall = instructorInstructionalSupportCallService.findOneById(instructorInstructionalSupportCallId);

        Workgroup workgroup = instructorSupportCall.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        instructorInstructionalSupportCallService.delete(instructorInstructionalSupportCallId);

        return instructorInstructionalSupportCallId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/studentInstructionalSupportCalls/{studentInstructionalSupportCallId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteStudentSupportCall(@PathVariable long studentInstructionalSupportCallId, HttpServletResponse httpResponse) {
        StudentSupportCall studentSupportCall = studentInstructionalSupportCallService.findOneById(studentInstructionalSupportCallId);

        Workgroup workgroup = studentSupportCall.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        studentInstructionalSupportCallService.delete(studentInstructionalSupportCallId);

        return studentInstructionalSupportCallId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/openStudentSupportCallReview", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Schedule openStudentSupportCallReview(@PathVariable long scheduleId, HttpServletResponse httpResponse) {


        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        schedule.setStudentSupportCallReviewOpen(true);

        return scheduleService.saveSchedule(schedule);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/openInstructorSupportCallReview", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public Schedule openInstructorSupportCallReview(@PathVariable long scheduleId, HttpServletResponse httpResponse) {


        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        schedule.setInstructorSupportCallReviewOpen(true);

        return scheduleService.saveSchedule(schedule);
    }
}