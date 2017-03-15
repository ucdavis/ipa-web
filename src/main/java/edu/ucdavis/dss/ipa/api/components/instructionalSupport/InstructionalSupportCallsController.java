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
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/supportCallStatus", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallStatusView getInstructionalSupportCallView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        return instructionalSupportViewFactory.createSupportCallStatusView(workgroupId, year);
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