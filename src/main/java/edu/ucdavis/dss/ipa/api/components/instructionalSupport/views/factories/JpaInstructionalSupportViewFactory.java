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
    @Inject
    ScheduleService scheduleService;
    @Inject ScheduleTermStateService scheduleTermStateService;
    @Inject SectionGroupService sectionGroupService;
    @Inject CourseService courseService;
    @Inject UserRoleService userRoleService;
    @Inject TeachingCallService teachingCallService;
    @Inject TeachingCallReceiptService teachingCallReceiptService;
    @Inject TeachingCallResponseService teachingCallResponseService;
    @Inject UserService userService;

    @Override
    public InstructionalSupportAssignmentView createAssignmentView(long workgroupId, long year, long userId) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        long scheduleId = schedule.getId();

        return new InstructionalSupportAssignmentView(userId, scheduleId);
    }
}
