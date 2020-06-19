package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class InstructionalSupportStudentFormsController {
    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportStaffService supportStaffService;
    @Inject StudentSupportPreferenceService studentSupportPreferenceService;
    @Inject StudentSupportCallResponseService studentSupportCallResponseService;
    @Inject ScheduleService scheduleService;
    @Inject SectionService sectionService;
    @Inject Authorization authorization;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportCallStudentFormView getInstructionalSupportCallStudentFormView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode) {
        authorizer.hasWorkgroupRoles(workgroupId, "studentMasters", "studentPhd", "instructionalSupport");

        User currentUser = userService.getOneByLoginId(authorization.getLoginId());
        SupportStaff supportStaff = supportStaffService.findByLoginId(currentUser.getLoginId());
        Long supportStaffId = 0L;

        if (supportStaff != null) {
            supportStaffId = supportStaff.getId();
        }

        return instructionalSupportViewFactory.createStudentFormView(workgroupId, year, shortTermCode, supportStaffId);
    }

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/sectionGroups/{sectionGroupId}/preferenceType/{preferenceType}/termCode/{termCode}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public StudentSupportPreference addPreference(@PathVariable long sectionGroupId, @PathVariable String preferenceType, @PathVariable String termCode) {
        Long workgroupId = sectionGroupService.getOneById(sectionGroupId).getCourse().getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "studentMasters", "studentPhd", "instructionalSupport");

        User currentUser = userService.getOneByLoginId(authorization.getLoginId());
        SupportStaff supportStaff = supportStaffService.findByLoginId(currentUser.getLoginId());

        StudentSupportPreference studentSupportPreference = new StudentSupportPreference();
        studentSupportPreference.setSectionGroup(sectionGroupService.getOneById(sectionGroupId));
        studentSupportPreference.setSupportStaff(supportStaff);
        studentSupportPreference.setType(preferenceType);
        studentSupportPreference.setComment("");
        studentSupportPreference.setTermCode(termCode);

        return studentSupportPreferenceService.create(studentSupportPreference);
    }

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/studentSupportCallResponses/{studentSupportCallResponseId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public StudentSupportCallResponse updateStudentSupportCallResponse(@PathVariable long studentSupportCallResponseId, @RequestBody StudentSupportCallResponse studentSupportCallResponseDTO) {
        StudentSupportCallResponse originalSupportCallResponse = studentSupportCallResponseService.findOneById(studentSupportCallResponseId);
        Long workgroupId = originalSupportCallResponse.getSchedule().getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "studentMasters", "studentPhd", "instructionalSupport");

        originalSupportCallResponse.setGeneralComments(studentSupportCallResponseDTO.getGeneralComments());
        originalSupportCallResponse.setTeachingQualifications(studentSupportCallResponseDTO.getTeachingQualifications());
        originalSupportCallResponse.setSubmitted(studentSupportCallResponseDTO.isSubmitted());
        originalSupportCallResponse.setEligibilityConfirmed(studentSupportCallResponseDTO.isEligibilityConfirmed());
        originalSupportCallResponse.setLanguageProficiency(studentSupportCallResponseDTO.getLanguageProficiency());
        
        if (originalSupportCallResponse.isCollectAvailabilityByCrn() || originalSupportCallResponse.isCollectAvailabilityByGrid()) {
            originalSupportCallResponse.setAvailabilityBlob(studentSupportCallResponseDTO.getAvailabilityBlob());
        }

        return studentSupportCallResponseService.update(originalSupportCallResponse);
    }

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/schedules/{scheduleId}/terms/{termCode}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public List<Long> updatePreferencesOrder(@PathVariable long scheduleId, @PathVariable String termCode, @RequestBody List<Long> preferenceIdsParams) {
        Long workgroupId = scheduleService.findById(scheduleId).getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "studentMasters", "studentPhd", "instructionalSupport");

        studentSupportPreferenceService.updatePriorities(preferenceIdsParams);

        return preferenceIdsParams;
    }

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/schedules/{scheduleId}/preferences/{supportStaffPreferenceId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public StudentSupportPreference updatePreference(@PathVariable long scheduleId, @PathVariable long supportStaffPreferenceId, @RequestBody StudentSupportPreference preferenceDTO) {
        Long workgroupId = scheduleService.findById(scheduleId).getWorkgroup().getId();
        authorizer.hasWorkgroupRoles(workgroupId, "studentMasters", "studentPhd", "instructionalSupport");

        StudentSupportPreference preference = studentSupportPreferenceService.findById(supportStaffPreferenceId);

        preference.setComment(preferenceDTO.getComment());

        return studentSupportPreferenceService.save(preference);
    }

    @RequestMapping(value = "/api/instructionalSupportStudentFormView/studentInstructionalSupportPreferences/{studentPreferenceId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deletePreference(@PathVariable long studentPreferenceId, HttpServletResponse httpResponse) {
        StudentSupportPreference studentPreference = studentSupportPreferenceService.findById(studentPreferenceId);

        if (studentPreference == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return studentPreferenceId;
        }

        authorizer.hasWorkgroupRoles(studentPreference.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId(), "studentMasters", "studentPhd", "instructionalSupport");

        studentSupportPreferenceService.delete(studentPreferenceId);

        return studentPreferenceId;
    }
}