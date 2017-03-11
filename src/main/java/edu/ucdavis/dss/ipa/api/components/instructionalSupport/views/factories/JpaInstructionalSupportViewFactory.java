package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

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
        List<SupportAssignment> supportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<UserRole> userRoles = workgroup.getUserRoles();
        List<SupportStaff> supportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);

        List<SupportStaff> mastersStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<SupportStaff> phdStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<SupportStaff> instructionalSupport = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();
        List<StudentSupportPreference> studentSupportPreferences = studentInstructionalSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportCallResponse> studentSupportCallResponses = studentInstructionalSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        for (SupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportAssignmentView(sectionGroups, courses, supportAssignments, supportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentSupportPreferences, studentSupportCallResponses, schedule);
    }

    @Override
    public InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        List<UserRole> userRoles = workgroup.getUserRoles();
        List<SupportStaff> supportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);
        List<StudentSupportCall> studentSupportCalls = studentInstructionalSupportCallService.findByScheduleId(schedule.getId());

        List<StudentSupportCallResponse> studentSupportCallResponses = new ArrayList<>();
        for (StudentSupportCall supportCall : studentSupportCalls) {
            studentSupportCallResponses.addAll(supportCall.getStudentSupportCallResponses());
        }


        List<InstructorSupportCall> instructorSupportCalls = instructorInstructionalSupportCallService.findByScheduleId(schedule.getId());

        List<InstructorSupportCallResponse> instructorSupportCallResponses = new ArrayList<>();
        for (InstructorSupportCall supportCall : instructorSupportCalls) {
            instructorSupportCallResponses.addAll(supportCall.getInstructorSupportCallResponses());
        }

        List<SupportStaff> mastersStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<SupportStaff> phdStudents = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<SupportStaff> instructionalSupport = instructionalSupportStaffService.findActiveByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();

        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroup.getId());
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();

        for (SupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportCallStatusView(schedule.getId(), supportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, studentSupportCalls, instructorSupportCalls, activeInstructors, teachingAssignments, studentSupportCallResponses, instructorSupportCallResponses);
    }

    @Override
    public InstructionalSupportCallStudentFormView createStudentFormView(long workgroupId, long year, String shortTermCode, long supportStaffId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Does the user have an associated supportStaff entity?
        User currentUser = userService.getOneByLoginId(Authorization.getLoginId());

        SupportStaff supportStaff = instructionalSupportStaffService.findByLoginId(currentUser.getLoginId());
        if (supportStaff == null) {
            return null;
        }

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        StudentSupportCall studentSupportCall = null;
        StudentSupportCallResponse studentSupportCallResponse = null;

        // Ensure the scheduleId and termCode match, and that the support Call contains a studentSupportCallResponse for the current user
        outerloop:
        for (StudentSupportCall slotStudentSupportCall : studentInstructionalSupportCallService.findByScheduleId(schedule.getId())) {
            if (slotStudentSupportCall.getTermCode().equals(termCode)) {

                for (StudentSupportCallResponse slotStudentSupportCallResponse : slotStudentSupportCall.getStudentSupportCallResponses()) {
                    if (slotStudentSupportCallResponse.getInstructionalSupportStaffIdentification() == supportStaff.getId() ) {
                        studentSupportCallResponse = slotStudentSupportCallResponse;
                        studentSupportCall = slotStudentSupportCall;

                        break outerloop;
                    }
                }

            }
        }

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCodeAndStudentSupportCallId(schedule.getId(), termCode, studentSupportCall.getId());
        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SupportAssignment> supportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportPreference> studentSupportPreferences = studentInstructionalSupportPreferenceService.findBySupportStaffIdAndStudentSupportCallId(supportStaffId, studentSupportCall.getId());

        return new InstructionalSupportCallStudentFormView(sectionGroups, courses, supportAssignments, studentSupportPreferences, schedule.getId(), supportStaffId, studentSupportCall, studentSupportCallResponse);
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

        InstructorSupportCall instructorSupportCall = null;
        InstructorSupportCallResponse instructorSupportCallResponse = null;

        // Set supportCall and supportCallResponse
        // Ensure the scheduleId and termCode match, and that the support Call contains a studentSupportCallResponse for the current user
        outerloop:
        for (InstructorSupportCall slotInstructorSupportCall : instructorInstructionalSupportCallService.findByScheduleId(schedule.getId())) {
            if (slotInstructorSupportCall.getTermCode().equals(termCode)) {

                for (InstructorSupportCallResponse slotInstructorSupportCallResponse : slotInstructorSupportCall.getInstructorSupportCallResponses()) {
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
        List<StudentSupportPreference> studentPreferences = new ArrayList<>();

        for (SectionGroup slotSectionGroup : sectionGroups) {
            for (StudentSupportPreference slotPreference : slotSectionGroup.getStudentInstructionalSupportCallPreferences()) {
                if ("teachingAssistant".equals(slotPreference.getType())) {
                    studentPreferences.add(slotPreference);
                }
            }
        }

        // Add instructor preferences associated to sectionGroups the instructor is teaching
        List<InstructorSupportPreference> instructorPreferences = new ArrayList<>();

        for (SectionGroup slotSectionGroup : sectionGroups) {
            instructorPreferences.addAll(slotSectionGroup.getInstructorSupportPreferences());
        }

        List<SupportStaff> supportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);

        return new InstructionalSupportCallInstructorFormView(sectionGroups, courses, studentPreferences, instructorPreferences, supportStaffList, schedule.getId(), instructorId, instructorSupportCall, instructorSupportCallResponse);
    }
}
