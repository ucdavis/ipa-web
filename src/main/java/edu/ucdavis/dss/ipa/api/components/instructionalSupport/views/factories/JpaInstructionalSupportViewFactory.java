package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallInstructorFormView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStatusView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportCallStudentFormView;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.InstructorSupportPreferenceService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.StudentSupportPreferenceService;
import edu.ucdavis.dss.ipa.services.SupportAppointmentService;
import edu.ucdavis.dss.ipa.services.SupportAssignmentService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JpaInstructionalSupportViewFactory implements InstructionalSupportViewFactory {
    @Inject WorkgroupService workgroupService;
    @Inject InstructorService instructorService;
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject SectionService sectionService;
    @Inject ActivityService activityService;
    @Inject CourseService courseService;
    @Inject UserService userService;
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject SupportStaffService supportStaffService;
    @Inject StudentSupportPreferenceService studentSupportPreferenceService;
    @Inject StudentSupportCallResponseService studentSupportCallResponseService;
    @Inject InstructorSupportCallResponseService instructorSupportCallResponseService;
    @Inject InstructorSupportPreferenceService instructorSupportPreferenceService;
    @Inject Authorization authorization;
    @Inject SupportAppointmentService supportAppointmentService;
    @Inject UserRoleService userRoleService;
    @Inject TeachingAssignmentService teachingAssignmentService;

    @Override
    public InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, String shortTermCode) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Calculate termCode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
        List<Activity> activities = activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);

        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SupportAssignment> supportAssignments = supportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        Set<SupportStaff> supportStaffList = new HashSet<SupportStaff>(userRoleService.findActiveSupportStaffByWorkgroupId(workgroupId));

        List<SupportStaff> assignedSupportStaff = supportStaffService.findByScheduleId(schedule.getId());

        List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<InstructorSupportPreference> instructorSupportPreferences = instructorSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<InstructorSupportCallResponse> instructorSupportCallResponses = instructorSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<SupportAppointment> supportAppointments = supportAppointmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);

        return new InstructionalSupportAssignmentView(sectionGroups, courses, supportAssignments, supportStaffList, assignedSupportStaff,
                studentSupportPreferences, studentSupportCallResponses, schedule, instructorSupportPreferences, instructorSupportCallResponses, supportAppointments, sections, activities);
    }

    @Override
    public InstructionalSupportCallStatusView createSupportCallStatusView(long workgroupId, long year, String shortTermCode) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        List<String> terms = new ArrayList<String>();

        terms.add("01");
        terms.add("02");
        terms.add("03");

        long academicYear = year;

        if (terms.indexOf(shortTermCode) > -1) {
            academicYear++;
        }

        String termCode = String.valueOf(academicYear) + shortTermCode;

        List<UserRole> userRoles = workgroup.getUserRoles();
        List<SupportStaff> supportStaffList = userRoleService.findActiveSupportStaffByWorkgroupId(workgroupId);
        List<Instructor> activeInstructors = instructorService.findActiveByWorkgroupId(workgroup.getId());

        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<InstructorSupportCallResponse> instructorSupportCallResponses = instructorSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);

        List<SupportStaff> mastersStudents = userRoleService.findActiveSupportStaffByWorkgroupIdAndRoleToken(workgroupId, "studentMasters");
        List<Long> mastersStudentIds = new ArrayList<>();
        List<SupportStaff> phdStudents = userRoleService.findActiveSupportStaffByWorkgroupIdAndRoleToken(workgroupId, "studentPhd");
        List<Long> phdStudentIds = new ArrayList<>();
        List<SupportStaff> instructionalSupport = userRoleService.findActiveSupportStaffByWorkgroupIdAndRoleToken(workgroupId, "instructionalSupport");
        List<Long> instructionalSupportIds = new ArrayList<>();

        for (SupportStaff supportStaff : mastersStudents) {
            mastersStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : phdStudents) {
            phdStudentIds.add(supportStaff.getId());
        }

        for (SupportStaff supportStaff : instructionalSupport) {
            instructionalSupportIds.add(supportStaff.getId());
        }

        return new InstructionalSupportCallStatusView(schedule.getId(), supportStaffList, mastersStudentIds, phdStudentIds, instructionalSupportIds, activeInstructors, studentSupportCallResponses, instructorSupportCallResponses);
    }

    @Override
    public InstructionalSupportCallStudentFormView createStudentFormView(long workgroupId, long year, String shortTermCode, long supportStaffId) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

        // Does the user have an associated supportStaff entity?
        User currentUser = userService.getOneByLoginId(authorization.getLoginId());

        // Calculate termcode from shortTermCode
        String termCode = "";

        if (Long.valueOf(shortTermCode) >= 5) {
            termCode = String.valueOf(year) + shortTermCode;
        } else {
            termCode = String.valueOf(year + 1) + shortTermCode;
        }

        StudentSupportCallResponse studentSupportCallResponse = studentSupportCallResponseService.findByScheduleIdAndSupportStaffIdAndTermCode(schedule.getId(), supportStaffId, termCode);

        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
        List<Activity> activities = activityService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);

        List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
        List<SupportAssignment> supportAssignments = supportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleIdAndTermCodeAndSupportStaffId(schedule.getId(), termCode, supportStaffId);

        return new InstructionalSupportCallStudentFormView(sectionGroups, courses, supportAssignments, studentSupportPreferences, schedule.getId(), supportStaffId, studentSupportCallResponse, sections, activities);
    }

    @Override
    public InstructionalSupportCallInstructorFormView createInstructorFormView(long workgroupId, long year, String shortTermCode, long instructorId) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);


        // Does the user have an associated instructor entity?
        User currentUser = userService.getOneByLoginId(authorization.getLoginId());

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

        InstructorSupportCallResponse instructorSupportCallResponse = instructorSupportCallResponseService.findByScheduleIdAndInstructorIdAndTermCode(schedule.getId(), instructorId, termCode);
        List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<Course> courses = courseService.findByWorkgroupIdAndYear(workgroupId, year);
        List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findByInstructorIdAndScheduleIdAndTermCode(instructorId, schedule.getId(), termCode);
        List<InstructorSupportPreference> instructorPreferences = instructorSupportPreferenceService.findByInstructorIdAndTermCode(instructorId, termCode);

        // Find all support staff and combine them
        Set<SupportStaff> activeStaffList = new HashSet<> (userRoleService.findActiveSupportStaffByWorkgroupIdAndPreferences(workgroupId, studentSupportPreferences));
        Set<SupportStaff> referencedSupportStaff = new HashSet<> ();

        for (InstructorSupportPreference preference : instructorPreferences) {
            referencedSupportStaff.add(preference.getSupportStaff());
        }

        Set<SupportStaff> supportStaffList = new HashSet<>();
        supportStaffList.addAll(activeStaffList);
        supportStaffList.addAll(referencedSupportStaff);

        return new InstructionalSupportCallInstructorFormView(sectionGroups, courses, studentSupportPreferences, instructorPreferences, supportStaffList, schedule.getId(), instructorId, instructorSupportCallResponse, studentSupportCallResponses, teachingAssignments);
    }
}
