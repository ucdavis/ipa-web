package edu.ucdavis.dss.ipa.api.components.instructionalSupport;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
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
@CrossOrigin
public class InstructionalSupportAssignmentsController {

    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject SupportStaffService supportStaffService;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportAssignmentView getInstructionalSupportAssignmentView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer", "senateInstructor", "federationInstructor", "studentPhd", "studentMasters", "instructionalSupport");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        return instructionalSupportViewFactory.createAssignmentView(workgroupId, year, shortTermCode);
    }

    @RequestMapping(value = "/api/instructionalSupportView/sectionGroups/{sectionGroupId}/instructionalSupportAssignments/{numberOfAssignments}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<SupportAssignment> addAssignmentSlots(@PathVariable long sectionGroupId, @PathVariable long numberOfAssignments, @RequestBody SupportAssignment supportAssignment, HttpServletResponse httpResponse) {

        List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();

        // Ensure submitted data looks reasonable
        if (supportAssignment.getAppointmentPercentage() > 0
            && supportAssignment.getAppointmentType().length() > 0
            && numberOfAssignments > 0
            && sectionGroupId > 0 ) {

            supportAssignments = supportAssignmentService.createMultiple(sectionGroupId, supportAssignment.getAppointmentType(), supportAssignment.getAppointmentPercentage(), numberOfAssignments);
        }

        Workgroup workgroup = sectionGroupService.getOneById(sectionGroupId).getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        return supportAssignments;
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{instructionalSupportAssignmentId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteAssignment(@PathVariable long instructionalSupportAssignmentId, HttpServletResponse httpResponse) {

        SupportAssignment supportAssignment = supportAssignmentService.findOneById(instructionalSupportAssignmentId);

        Workgroup workgroup = supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // Ensure the assignment is unassigned.
        if (supportAssignment.getSupportStaff() != null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        supportAssignmentService.delete(instructionalSupportAssignmentId);

        return instructionalSupportAssignmentId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{assignmentId}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public SupportAssignment assignStaffToSlot(@PathVariable long assignmentId, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        SupportAssignment supportAssignment = supportAssignmentService.findOneById(assignmentId);
        SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);

        Workgroup workgroup = supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        supportAssignment.setSupportStaff(supportStaff);

        return supportAssignmentService.save(supportAssignment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/sectionGroups/{sectionGroupId}/assignmentType/{type}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public SupportAssignment assignStaffToSectionGroupSlot(@PathVariable long sectionGroupId, @PathVariable String type, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        SupportStaff supportStaff = supportStaffService.findOneById(supportStaffId);

        if (supportStaff == null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        SupportAssignment supportAssignment = null;

        // Find a supportAssignment of specified type, that is not already filled
        for (SupportAssignment slotSupportAssignment : sectionGroup.getSupportAssignments()) {
            if (type.equals(slotSupportAssignment.getAppointmentType()) && slotSupportAssignment.getSupportStaff() == null) {
                supportAssignment = slotSupportAssignment;
            }
        }

        // If one is not found, make a new slot
        if (supportAssignment == null) {
            supportAssignment = new SupportAssignment();
            supportAssignment.setAppointmentPercentage(50);
            supportAssignment.setAppointmentType(type);
            supportAssignment.setSectionGroup(sectionGroup);
        }


        supportAssignment.setSupportStaff(supportStaff);

        return supportAssignmentService.save(supportAssignment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{instructionalSupportAssignmentId}/unassign", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public SupportAssignment removeStaffFromSlot(@PathVariable long instructionalSupportAssignmentId, HttpServletResponse httpResponse) {
        SupportAssignment supportAssignment = supportAssignmentService.findOneById(instructionalSupportAssignmentId);

        Workgroup workgroup = supportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        supportAssignment.setSupportStaff(null);

        return supportAssignmentService.save(supportAssignment);
    }
}