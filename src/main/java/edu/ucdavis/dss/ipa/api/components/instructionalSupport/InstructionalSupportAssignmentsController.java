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
@CrossOrigin // TODO: make CORS more specific depending on profile
public class InstructionalSupportAssignmentsController {

    @Inject InstructionalSupportViewFactory instructionalSupportViewFactory;
    @Inject UserService userService;
    @Inject InstructorService instructorService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;

    @RequestMapping(value = "/api/instructionalSupportView/workgroups/{workgroupId}/years/{year}/termCode/{shortTermCode}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public InstructionalSupportAssignmentView getInstructionalSupportAssignmentView(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String shortTermCode, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        return instructionalSupportViewFactory.createAssignmentView(workgroupId, year, shortTermCode);
    }

    @RequestMapping(value = "/api/instructionalSupportView/sectionGroups/{sectionGroupId}/instructionalSupportAssignments/{numberOfAssignments}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public List<InstructionalSupportAssignment> addAssignmentSlots(@PathVariable long sectionGroupId, @PathVariable long numberOfAssignments, @RequestBody InstructionalSupportAssignment instructionalSupportAssignment, HttpServletResponse httpResponse) {

        List<InstructionalSupportAssignment> instructionalSupportAssignments = new ArrayList<InstructionalSupportAssignment>();

        // Ensure submitted data looks reasonable
        if (instructionalSupportAssignment.getAppointmentPercentage() > 0
            && instructionalSupportAssignment.getAppointmentType().length() > 0
            && numberOfAssignments > 0
            && sectionGroupId > 0 ) {

            instructionalSupportAssignments = instructionalSupportAssignmentService.createMultiple(sectionGroupId, instructionalSupportAssignment.getAppointmentType(), instructionalSupportAssignment.getAppointmentPercentage(), numberOfAssignments);
        }

        Workgroup workgroup = sectionGroupService.getOneById(sectionGroupId).getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        return instructionalSupportAssignments;
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{instructionalSupportAssignmentId}", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public Long deleteAssignment(@PathVariable long instructionalSupportAssignmentId, HttpServletResponse httpResponse) {

        InstructionalSupportAssignment instructionalSupportAssignment = instructionalSupportAssignmentService.findOneById(instructionalSupportAssignmentId);

        Workgroup workgroup = instructionalSupportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        // Ensure the assignment is unassigned.
        if (instructionalSupportAssignment.getInstructionalSupportStaff() != null) {
            httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        instructionalSupportAssignmentService.delete(instructionalSupportAssignmentId);

        return instructionalSupportAssignmentId;
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{assignmentId}/supportStaff/{supportStaffId}", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public InstructionalSupportAssignment assignStaffToSlot(@PathVariable long assignmentId, @PathVariable long supportStaffId, HttpServletResponse httpResponse) {
        InstructionalSupportAssignment instructionalSupportAssignment = instructionalSupportAssignmentService.findOneById(assignmentId);
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(supportStaffId);

        Workgroup workgroup = instructionalSupportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        instructionalSupportAssignment.setInstructionalSupportStaff(instructionalSupportStaff);

        return instructionalSupportAssignmentService.save(instructionalSupportAssignment);
    }

    @RequestMapping(value = "/api/instructionalSupportView/instructionalSupportAssignments/{instructionalSupportAssignmentId}/unassign", method = RequestMethod.DELETE, produces = "application/json")
    @ResponseBody
    public InstructionalSupportAssignment removeStaffFromSlot(@PathVariable long instructionalSupportAssignmentId, HttpServletResponse httpResponse) {
        InstructionalSupportAssignment instructionalSupportAssignment = instructionalSupportAssignmentService.findOneById(instructionalSupportAssignmentId);

        Workgroup workgroup = instructionalSupportAssignment.getSectionGroup().getCourse().getSchedule().getWorkgroup();
        Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        instructionalSupportAssignment.setInstructionalSupportStaff(null);

        return instructionalSupportAssignmentService.save(instructionalSupportAssignment);
    }
}