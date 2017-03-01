package edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories;

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

    @Override
    public TeachingCallStatusView createTeachingCallStatusView(long workgroupId, long year) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        long scheduleId = schedule.getId();
        List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
        List<TeachingCallReceipt> teachingCallReceipts = schedule.getTeachingCallReceipts();
        List<Long> senateInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "senateInstructor");
        List<Long> federationInstructorIds = userRoleService.getInstructorsByWorkgroupIdAndRoleToken(workgroupId, "federationInstructor");

        return new TeachingCallStatusView(instructors, teachingCallReceipts, scheduleId, senateInstructorIds, federationInstructorIds);
    }
}
