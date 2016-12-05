package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentExcelView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructionalSupportViewFactory implements InstructionalSupportViewFactory {
    @Inject WorkgroupService workgroupService;
    @Inject InstructorService instructorService;
    @Inject ScheduleInstructorNoteService scheduleInstructorNoteService;
    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject CourseService courseService;
    @Inject UserService userService;
    @Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;
    @Inject UserRoleService userRoleService;
    @Inject StudentInstructionalSupportPreferenceService studentInstructionalSupportPreferenceService;

    @Override
    public InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, String shortTermCode) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<InstructionalSupportAssignment> instructionalSupportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<UserRole> userRoles = workgroup.getUserRoles();
        List<InstructionalSupportStaff> instructionalSupportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);

        List<InstructionalSupportStaff> mastersStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> phdStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> instructionalSupport = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();
        List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences = studentInstructionalSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        for (InstructionalSupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportAssignmentView(sectionGroups, courses, instructionalSupportAssignments, instructionalSupportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentInstructionalSupportPreferences);
    }

    @Override
    public InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        List<UserRole> userRoles = workgroup.getUserRoles();
        List<InstructionalSupportStaff> instructionalSupportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);
        List<StudentInstructionalSupportCall> studentInstructionalSupportCalls = studentInstructionalSupportCallService.findByScheduleId(schedule.getId());

        List<InstructionalSupportStaff> mastersStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> phdStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> instructionalSupport = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();

        for (InstructionalSupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }


        return new InstructionalSupportCallStatusView(schedule.getId(), instructionalSupportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentInstructionalSupportCalls);
    }

    @Override
    public InstructionalSupportCallStudentFormView createStudentFormView(long workgroupId, long year, String shortTermCode, long supportStaffId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        StudentInstructionalSupportCall studentSupportCall = null;

        for (StudentInstructionalSupportCall slotStudentInstructionalSupportCall : studentInstructionalSupportCallService.findByScheduleId(schedule.getId())) {
            if (slotStudentInstructionalSupportCall.getTermCode().equals(termCode)) {
                studentSupportCall = slotStudentInstructionalSupportCall;
            }
        }

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCodeAndStudentSupportCallId(schedule.getId(), termCode, studentSupportCall.getId());
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<InstructionalSupportAssignment> instructionalSupportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences = studentInstructionalSupportPreferenceService.findBySupportStaffIdAndStudentSupportCallId(supportStaffId, studentSupportCall.getId());

        return new InstructionalSupportCallStudentFormView(sectionGroups, courses, instructionalSupportAssignments, studentInstructionalSupportPreferences, schedule.getId(), supportStaffId, studentSupportCall);
    }
}
