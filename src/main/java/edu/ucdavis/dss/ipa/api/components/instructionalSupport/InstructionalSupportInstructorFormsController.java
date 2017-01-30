package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
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
public class InstructionalSupportInstructorFormsController {

    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject InstructorInstructionalSupportPreferenceService instructorInstructionalSupportPreferenceService;
    @Inject InstructorInstructionalSupportCallResponseService instructorInstructionalSupportCallResponseService;
    @Inject ScheduleService scheduleService;

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallInstructorFormView getInstructionalSupportCallStudentFormView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());

        return instructionalSupportViewFactory.createInstructorFormView(workgroupId, year, shortTermCode, instructor.getId());
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/supportCalls/{supportCallId}/sectionGroups/{sectionGroupId}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public InstructorInstructionalSupportPreference addPreference(@PathVariable long supportCallId, @PathVariable long sectionGroupId, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        Long workgroupId = sectionGroupService.getOneById(sectionGroupId).getCourse().getSchedule().getWorkgroup().getId();

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());
        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());

        return instructorInstructionalSupportPreferenceService.create(supportStaffId, instructor.getId(), supportCallId, sectionGroupId);
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/instructorSupportCallResponses/{instructorSupportCallResponseId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public InstructorInstructionalSupportCallResponse updateInstructorSupportCallResponse(@PathVariable long instructorSupportCallResponseId, @RequestBody InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponseDTO, HttpServletResponse httpResponse) {
        InstructorInstructionalSupportCallResponse originalSupportCallResponse = instructorInstructionalSupportCallResponseService.findOneById(instructorSupportCallResponseId);
        Long workgroupId = originalSupportCallResponse.getInstructorInstructionalSupportCall().getSchedule().getWorkgroup().getId();

        originalSupportCallResponse.setGeneralComments(instructorInstructionalSupportCallResponseDTO.getGeneralComments());
        originalSupportCallResponse.setSubmitted(instructorInstructionalSupportCallResponseDTO.isSubmitted());

        return instructorInstructionalSupportCallResponseService.update(originalSupportCallResponse);
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/schedules/{scheduleId}/sectionGroups/{sectionGroupId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public List<Long> updatePreferencesOrder(@PathVariable long scheduleId, @PathVariable long sectionGroupId, @RequestBody List<Long> preferenceIdsParams, HttpServletResponse httpResponse) {
        Long workgroupId = sectionGroupService.getOneById(scheduleId).getCourse().getSchedule().getWorkgroup().getId();

        instructorInstructionalSupportPreferenceService.updatePriorities(preferenceIdsParams);

        return preferenceIdsParams;
    }

    @RequestMapping(value = "/api/instructionalSupportInstructorFormView/instructorInstructionalSupportPreferences/{instructorPreferenceId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deletePreference(@PathVariable long instructorPreferenceId, HttpServletResponse httpResponse) {
        instructorInstructionalSupportPreferenceService.delete(instructorPreferenceId);

        return instructorPreferenceId;
    }
}