package edu.ucdavis.dss.ipa.api.components.assignment;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.factories.AssignmentViewFactory;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.Authorizer;

import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class AssignmentViewController {
    @Inject AssignmentViewFactory assignmentViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject Authorization authorization;
    @Inject Authorizer authorizer;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportStaffService supportStaffService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject UserRoleService userRoleService;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @RequestMapping(value = "/api/assignmentView/{workgroupId}/{year}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public AssignmentView getAssignmentViewByCode(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "instructor");

        User currentUser = userService.getOneByLoginId(authorization.getLoginId());

        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());
        long instructorId = 0;
        // Academic coordinators will not have instructors associated to their user
        if (instructor != null) {
            instructorId = instructor.getId();
        }

        return assignmentViewFactory.createAssignmentView(workgroupId, year, currentUser.getId(), instructorId);
    }


    @RequestMapping(value = "/api/assignmentView/sectionGroups/{sectionGroupId}/supportStaff/{supportStaffId}/assignAI", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public TeachingAssignment assignStudentToAssociateInstructor(@PathVariable long sectionGroupId, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);

        if (supportStaff == null || sectionGroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // Ensure instructor object has been created
        Instructor instructor = instructorService.findOrCreate(supportStaff.getFirstName(), supportStaff.getLastName(), supportStaff.getEmail(), supportStaff.getLoginId(), workgroup.getId());

        // Ensure lecturer role is given
        userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(supportStaff.getLoginId(), workgroup.getId(), "instructor");

        // Assign supportStaff to AI
        TeachingAssignment teachingAssignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
        teachingAssignment.setApproved(true);

        // Remove placeholderAI flag
        sectionGroupService.save(sectionGroup);

        return teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);
    }

    @RequestMapping(value = "/api/assignmentView/workgroups/{workgroupId}/years/{year}/generateExcel", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year,
                                             HttpServletRequest httpRequest) {
        authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        String url = ipaUrlApi + "/download/assignmentView/workgroups/" + workgroupId + "/years/"+ year +"/excel";
        String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        Map<String, String> map = new HashMap<>();
        map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress));

        return map;
    }

    /**
     * Exports a schedule as an Excel .xls file
     *
     * @param workgroupId
     * @param year
     * @param salt
     * @param encrypted
     * @param httpRequest
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/download/assignmentView/workgroups/{workgroupId}/years/{year}/excel/{salt}/{encrypted}")
    public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
                              @PathVariable String salt, @PathVariable String encrypted,
                              HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
        long TIMEOUT = 30L; // In seconds

        String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null) {
            ipAddress = httpRequest.getRemoteAddr();
        }

        boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);

        if (isValidUrl) {
            return assignmentViewFactory.createAssignmentExcelView(workgroupId, year);
        } else {
            httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            return null;
        }
    }
}
