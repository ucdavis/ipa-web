package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentExcelView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
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
    @Inject StudentInstructionalSupportCallResponseService studentInstructionalSupportCallResponseService;
    @Inject InstructorInstructionalSupportCallService instructorInstructionalSupportCallService;
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
        List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses = studentInstructionalSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        for (InstructionalSupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportAssignmentView(sectionGroups, courses, instructionalSupportAssignments, instructionalSupportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentInstructionalSupportPreferences, studentInstructionalSupportCallResponses, schedule);
    }

    @Override
    public InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        List<UserRole> userRoles = workgroup.getUserRoles();
        List<InstructionalSupportStaff> instructionalSupportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);
        List<StudentInstructionalSupportCall> studentInstructionalSupportCalls = studentInstructionalSupportCallService.findByScheduleId(schedule.getId());

        List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses = new ArrayList<>();
        for (StudentInstructionalSupportCall supportCall : studentInstructionalSupportCalls) {
            studentInstructionalSupportCallResponses.addAll(supportCall.getStudentInstructionalSupportCallResponses());
        }


        List<InstructorInstructionalSupportCall> instructorInstructionalSupportCalls = instructorInstructionalSupportCallService.findByScheduleId(schedule.getId());

        List<InstructorInstructionalSupportCallResponse> instructorInstructionalSupportCallResponses = new ArrayList<>();
        for (InstructorInstructionalSupportCall supportCall : instructorInstructionalSupportCalls) {
            instructorInstructionalSupportCallResponses.addAll(supportCall.getInstructorInstructionalSupportCallResponses());
        }

        List<InstructionalSupportStaff> mastersStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> phdStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<InstructionalSupportStaff> instructionalSupport = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();

        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroup.getId());
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();

        for (InstructionalSupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (InstructionalSupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportCallStatusView(schedule.getId(), instructionalSupportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentInstructionalSupportCalls, instructorInstructionalSupportCalls, activeInstructors, teachingAssignments, studentInstructionalSupportCallResponses, instructorInstructionalSupportCallResponses);
    }

    @Override
    public InstructionalSupportCallStudentFormView createStudentFormView(long workgroupId, long year, String shortTermCode, long supportStaffId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Does the user have an associated instructionalSupportStaff entity?
        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findByLoginId(currentUser.getLoginId());
        if (instructionalSupportStaff == null) {
            return null;
        }

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        StudentInstructionalSupportCall studentSupportCall = null;
        StudentInstructionalSupportCallResponse studentSupportCallResponse = null;

        // Ensure the scheduleId and termCode match, and that the support Call contains a studentSupportCallResponse for the current user
        outerloop:
        for (StudentInstructionalSupportCall slotStudentInstructionalSupportCall : studentInstructionalSupportCallService.findByScheduleId(schedule.getId())) {
            if (slotStudentInstructionalSupportCall.getTermCode().equals(termCode)) {

                for (StudentInstructionalSupportCallResponse slotStudentInstructionalSupportCallResponse : slotStudentInstructionalSupportCall.getStudentInstructionalSupportCallResponses()) {
                    if (slotStudentInstructionalSupportCallResponse.getInstructionalSupportStaffIdentification() == instructionalSupportStaff.getId() ) {
                        studentSupportCallResponse = slotStudentInstructionalSupportCallResponse;
                        studentSupportCall = slotStudentInstructionalSupportCall;

                        break outerloop;
                    }
                }

            }
        }

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCodeAndStudentSupportCallId(schedule.getId(), termCode, studentSupportCall.getId());
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<InstructionalSupportAssignment> instructionalSupportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences = studentInstructionalSupportPreferenceService.findBySupportStaffIdAndStudentSupportCallId(supportStaffId, studentSupportCall.getId());

        return new InstructionalSupportCallStudentFormView(sectionGroups, courses, instructionalSupportAssignments, studentInstructionalSupportPreferences, schedule.getId(), supportStaffId, studentSupportCall, studentSupportCallResponse);
    }

    @Override
    public InstructionalSupportCallInstructorFormView createInstructorFormView(long workgroupId, long year, String shortTermCode, long instructorId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Does the user have an associated instructor entity?
        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        Instructor instructor = instructorService.getOneByLoginId(currentUser.getLoginId());
        if (instructor == null) {
            return null;
        }

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        InstructorInstructionalSupportCall instructorSupportCall = null;
        InstructorInstructionalSupportCallResponse instructorSupportCallResponse = null;

        // Set supportCall and supportCallResponse
        // Ensure the scheduleId and termCode match, and that the support Call contains a studentSupportCallResponse for the current user
        outerloop:
        for (InstructorInstructionalSupportCall slotInstructorSupportCall : instructorInstructionalSupportCallService.findByScheduleId(schedule.getId())) {
            if (slotInstructorSupportCall.getTermCode().equals(termCode)) {

                for (InstructorInstructionalSupportCallResponse slotInstructorSupportCallResponse : slotInstructorSupportCall.getInstructorInstructionalSupportCallResponses()) {
                    if (slotInstructorSupportCallResponse.getInstructorIdentification() == instructor.getId() ) {
                        instructorSupportCallResponse = slotInstructorSupportCallResponse;
                        instructorSupportCall = slotInstructorSupportCall;

                        break outerloop;
                    }
                }

            }
        }

        // Set sectionGroups and Courses
        List<SectionGroup> sectionGroups = new ArrayList<>();
        List<Course> courses = new ArrayList<>();
        List<Long> courseIds = new ArrayList<>();

        for (TeachingAssignment teachingAssignment : instructor.getTeachingAssignments()) {
            if (termCode.equals(teachingAssignment.getTermCode()) && teachingAssignment.isApproved()) {
                sectionGroups.add(teachingAssignment.getSectionGroup());

                // Only add unique courses
                Course slotCourse = teachingAssignment.getSectionGroup().getCourse();

                if (courseIds.indexOf(slotCourse.getId()) < 0) {
                    courses.add(slotCourse);
                    courseIds.add(slotCourse.getId());
                }
            }
        }

        // Add student preferences associated to sectionGroups the instructor is teaching
        List<StudentInstructionalSupportPreference> studentPreferences = new ArrayList<>();

        for (SectionGroup slotSectionGroup : sectionGroups) {
            for (StudentInstructionalSupportPreference slotPreference : slotSectionGroup.getStudentInstructionalSupportCallPreferences()) {
                if ("teachingAssistant".equals(slotPreference.getType())) {
                    studentPreferences.add(slotPreference);
                }
            }
        }

        // Add instructor preferences associated to sectionGroups the instructor is teaching
        List<InstructorInstructionalSupportPreference> instructorPreferences = new ArrayList<>();

        for (SectionGroup slotSectionGroup : sectionGroups) {
            instructorPreferences.addAll(slotSectionGroup.getInstructorInstructionalSupportPreferences());
        }

        List<InstructionalSupportStaff> instructionalSupportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);

        return new InstructionalSupportCallInstructorFormView(sectionGroups, courses, studentPreferences, instructorPreferences, instructionalSupportStaffList, schedule.getId(), instructorId, instructorSupportCall, instructorSupportCallResponse);
    }
}
