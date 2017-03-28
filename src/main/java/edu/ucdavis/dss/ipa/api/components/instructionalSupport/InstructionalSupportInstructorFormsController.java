package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class InstructionalSupportInstructorFormsController {

    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupService sectionGroupService;
    @Inject
    SupportAssignmentService supportAssignmentService;
    @Inject
    SupportStaffService supportStaffService;
    @Inject
    InstructorSupportPreferenceService instructorSupportPreferenceService;
    @Inject
    InstructorSupportCallResponseService instructorSupportCallResponseService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallInstructorFormView getInstructionalSupportCallInstructorFormView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode, HttpServletResponse httpResponse) {
        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());

        return instructionalSupportViewFactory.createInstructorFormView(workgroupId, year, shortTermCode, instructor.getId());
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/sectionGroups/{sectionGroupId}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public InstructorSupportPreference addPreference(@PathVariable long sectionGroupId, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        Long workgroupId = sectionGroupService.getOneById(sectionGroupId).getCourse().getSchedule().getWorkgroup().getId();
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "studentPhd", "studentMasters", "instructionalSupport");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());

        return instructorSupportPreferenceService.create(supportStaffId, instructor.getId(), sectionGroupId);
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/instructorSupportCallResponses/{instructorSupportCallResponseId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public InstructorSupportCallResponse updateInstructorSupportCallResponse(@PathVariable long instructorSupportCallResponseId, @RequestBody InstructorSupportCallResponse instructorSupportCallResponseDTO, HttpServletResponse httpResponse) {
        InstructorSupportCallResponse originalSupportCallResponse = instructorSupportCallResponseService.findOneById(instructorSupportCallResponseId);
        Long workgroupId = originalSupportCallResponse.getSchedule().getWorkgroup().getId();
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "studentPhd", "studentMasters", "instructionalSupport");

        originalSupportCallResponse.setGeneralComments(instructorSupportCallResponseDTO.getGeneralComments());
        originalSupportCallResponse.setSubmitted(instructorSupportCallResponseDTO.isSubmitted());

        return instructorSupportCallResponseService.update(originalSupportCallResponse);
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/schedules/{scheduleId}/sectionGroups/{sectionGroupId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public List<Long> updatePreferencesOrder(@PathVariable long scheduleId, @PathVariable long sectionGroupId, @RequestBody List<Long> preferenceIdsParams, HttpServletResponse httpResponse) {
        Long workgroupId = scheduleService.findById(scheduleId).getWorkgroup().getId();
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "studentPhd", "studentMasters", "instructionalSupport");

        instructorSupportPreferenceService.updatePriorities(preferenceIdsParams);

        return preferenceIdsParams;
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/instructorInstructionalSupportPreferences/{instructorPreferenceId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deletePreference(@PathVariable long instructorPreferenceId, HttpServletResponse httpResponse) {
        instructorSupportPreferenceService.delete(instructorPreferenceId);

        return instructorPreferenceId;
    }
}