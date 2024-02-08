package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories.InstructionalSupportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.services.*;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class InstructionalSupportAssignmentsController {
    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject SupportStaffService supportStaffService;
    @Inject Authorizer authorizer;
    @Inject SectionService sectionService;
    @Inject SupportAppointmentService supportAppointmentService;
    @Inject ScheduleService scheduleService;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportAssignmentView getInstructionalSupportAssignmentView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

        return instructionalSupportViewFactory.createAssignmentView(workgroupId, year, shortTermCode);
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{supportAssignmentId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteAssignment(@PathVariable long supportAssignmentId, HttpServletResponse httpResponse) {
        SupportAssignment supportAssignment = supportAssignmentService.findOneById(supportAssignmentId);
        Workgroup workgroup = null;

        if (supportAssignment.getSectionGroup() != null) {
            workgroup = supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        } else if (supportAssignment.getSection() != null){
            workgroup = supportAssignment.getSection().getSectionGroup().getCourse().getSchedule().getWorkgroup();
        } else {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        supportAssignmentService.delete(supportAssignmentId);

        return supportAssignmentId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/sectionGroups/{sectionGroupId}/assignmentType/{type}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public SupportAssignment assignStaffToSectionGroup(@PathVariable long sectionGroupId, @PathVariable String type, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);

        if (supportStaff == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        SupportAssignment supportAssignment = new SupportAssignment();
        supportAssignment.setAppointmentType(type);
        supportAssignment.setSectionGroup(sectionGroup);
        supportAssignment.setSupportStaff(supportStaff);

        return supportAssignmentService.save(supportAssignment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/sections/{sectionId}/assignmentType/{type}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public SupportAssignment assignStaffToSection(@PathVariable long sectionId, @PathVariable String type, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        Section section = sectionService.getOneById(sectionId);

        Workgroup workgroup = section.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);

        if (supportStaff == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        SupportAssignment supportAssignment = new SupportAssignment();
        supportAssignment.setAppointmentType(type);
        supportAssignment.setSection(section);
        supportAssignment.setSupportStaff(supportStaff);

        return supportAssignmentService.save(supportAssignment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/schedules/{scheduleId}", method = RequestMethod.PUT, produces = "application/json")
    @ResponseBody
    public SupportAppointment updateSupportStaffAppointment(@PathVariable long scheduleId, @RequestBody SupportAppointment supportAppointment, HttpServletResponse httpResponse) {
        Schedule schedule = scheduleService.findById(scheduleId);
        SupportStaff supportStaff = supportStaffService.findOneById(supportAppointment.getSupportStaff().getId());

        if (schedule == null || supportStaff == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        Workgroup workgroup = schedule.getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        supportAppointment.setSchedule(schedule);
        supportAppointment.setSupportStaff(supportStaff);

        return supportAppointmentService.createOrUpdate(supportAppointment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year,
                                             @PathVariable String shortTermCode, HttpServletRequest httpRequest) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url = ipaUrlApi + "/download/instructionalSupportView/workgroups/" + workgroupId + "/years/" + year +
            "/termCode/" + shortTermCode + "/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    @RequestMapping(value = "/download/instructionalSupportView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
                              @PathVariable String shortTermCode, @PathVariable String salt,
                              @PathVariable String encrypted, HttpServletRequest httpRequest,
                              HttpServletResponse httpResponse) throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return instructionalSupportViewFactory.createInstructionalSupportExcelView(workgroupId, year,
                shortTermCode);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }
}