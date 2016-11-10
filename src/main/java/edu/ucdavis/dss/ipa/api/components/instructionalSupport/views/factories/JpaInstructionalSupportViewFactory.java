package edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.factories;

import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentExcelView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.AssignmentView;
import edu.ucdavis.dss.ipa.api.components.instructionalSupport.views.InstructionalSupportAssignmentView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaInstructionalSupportViewFactory implements InstructionalSupportViewFactory {
    @Inject
    WorkgroupService workgroupService;
    @Inject
    InstructorService instructorService;
    @Inject
    ScheduleInstructorNoteService scheduleInstructorNoteService;
    @Inject
    TeachingAssignmentService teachingAssignmentService;
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject CourseService courseService;
    @Inject UserService userService;
    @Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;

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


        List<InstructionalSupportStaff> instructionalSupportStaffList = instructionalSupportStaffService.findActiveByWorkgroupId(workgroupId);

        return new InstructionalSupportAssignmentView(sectionGroups, courses, instructionalSupportAssignments, instructionalSupportStaffList);
    }
}
