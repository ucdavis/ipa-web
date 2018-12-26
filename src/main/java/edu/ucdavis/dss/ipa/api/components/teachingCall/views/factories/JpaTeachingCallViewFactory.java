package edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallFormView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallStatusView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaTeachingCallViewFactory implements TeachingCallViewFactory {
    @Inject ScheduleService scheduleService;
    @Inject UserRoleService userRoleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructorService instructorService;
    @Inject TeachingCallResponseService teachingCallResponseService;
    @Inject InstructorTypeService instructorTypeService;
    @Inject UserService userService;

    @Override
    public TeachingCallStatusView createTeachingCallStatusView(long workgroupId, long year) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        long scheduleId = schedule.getId();

        List<Instructor> instructors = userRoleService.getInstructorsByScheduleIdAndWorkgroupId(scheduleId, workgroupId);
        List<TeachingCallReceipt> teachingCallReceipts = schedule.getTeachingCallReceipts();
        List<UserRole> userRoles = userRoleService.findByWorkgroupIdAndRoleToken(workgroupId, "instructor");
        List<User> users = userService.findAllByWorkgroupAndRoleToken(schedule.getWorkgroup(), "instructor");
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

        return new TeachingCallStatusView(instructors, teachingCallReceipts, scheduleId, userRoles, instructorTypes, users);
    }

    @Override
    public TeachingCallFormView createTeachingCallFormView(long workgroupId, long year, long userId, long instructorId) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        long scheduleId = schedule.getId();
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);
        Instructor instructor = instructorService.getOneById(instructorId);

        TeachingCallReceipt teachingCallReceipt = null;

        if (instructor != null) {
            for (TeachingCallReceipt slotTeachingCallReceipt : instructor.getTeachingCallReceipts()){
                if (slotTeachingCallReceipt.getSchedule().getId() == scheduleId) {
                    teachingCallReceipt = slotTeachingCallReceipt;
                }
            }
        }


        List<TeachingCallResponse> teachingCallResponses = teachingCallResponseService.findOrCreateByScheduleIdAndInstructorId(scheduleId, instructorId);

        return new TeachingCallFormView(courses, sectionGroups, schedule.getTeachingAssignments(), instructor,
                teachingCallReceipt, teachingCallResponses, userId, instructorId, scheduleId);
    }

}
