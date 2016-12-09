package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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
    public StudentInstructionalSupportCall addStudentSupportCall(@PathVariable long scheduleId, @RequestBody StudentInstructionalSupportCall studentInstructionalSupportCall, HttpServletResponse httpResponse) {


        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        studentInstructionalSupportCall.setSchedule(schedule);

        return studentInstructionalSupportCallService.findOrCreate(studentInstructionalSupportCall);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}/instructorInstructionalSupportCalls", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public InstructorInstructionalSupportCall addInstructorSupportCall(@PathVariable long scheduleId, @RequestBody InstructorInstructionalSupportCall instructorInstructionalSupportCall, HttpServletResponse httpResponse) {
        Workgroup workgroup = scheduleService.findById(scheduleId).getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        Schedule schedule = scheduleService.findById(scheduleId);
        instructorInstructionalSupportCall.setSchedule(schedule);

        return instructorInstructionalSupportCallService.findOrCreate(instructorInstructionalSupportCall);
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructorInstructionalSupportCalls/{instructorInstructionalSupportCallId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteInstructorSupportCall(@PathVariable long instructorInstructionalSupportCallId, HttpServletResponse httpResponse) {
        InstructorInstructionalSupportCall instructorInstructionalSupportCall = instructorInstructionalSupportCallService.findOneById(instructorInstructionalSupportCallId);

        Workgroup workgroup = instructorInstructionalSupportCall.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        instructorInstructionalSupportCallService.delete(instructorInstructionalSupportCallId);

        return instructorInstructionalSupportCallId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/studentInstructionalSupportCalls/{studentInstructionalSupportCallId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteStudentSupportCall(@PathVariable long studentInstructionalSupportCallId, HttpServletResponse httpResponse) {
        StudentInstructionalSupportCall studentInstructionalSupportCall = studentInstructionalSupportCallService.findOneById(studentInstructionalSupportCallId);

        Workgroup workgroup = studentInstructionalSupportCall.getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        studentInstructionalSupportCallService.delete(studentInstructionalSupportCallId);

        return studentInstructionalSupportCallId;
    }
}