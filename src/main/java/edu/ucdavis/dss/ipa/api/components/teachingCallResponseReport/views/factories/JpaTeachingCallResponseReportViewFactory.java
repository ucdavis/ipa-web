package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportExcelView;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaTeachingCallResponseReportViewFactory implements TeachingCallResponseReportViewFactory {
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;

    @Override
    public TeachingCallResponseReportView createTeachingCallResponseReportView(long workgroupId, long year) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        if(schedule == null) {
            // No such schedule, so no teaching call to report on
            return null;
        }

        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);

        List<TeachingCallResponse> teachingCallResponses = schedule.getTeachingCallResponses();
        List<TeachingCallReceipt> teachingCallReceipts = schedule.getTeachingCallReceipts();
        List<Instructor> instructors = new ArrayList<>();

        for (TeachingAssignment teachingAssignment : teachingAssignments) {
            Instructor instructor = teachingAssignment.getInstructor();

            // Only add new instances of the instructor
            if (instructors.indexOf(instructor) == -1) {
                instructors.add(instructor);
            }
        }

        return new TeachingCallResponseReportView(courses, sectionGroups, teachingAssignments, teachingCallReceipts, teachingCallResponses, instructors, schedule);
    }

    @Override
    public View createTeachingCallResponseReportExcelView(long workgroupId, long year) {
        TeachingCallResponseReportView teachingCallResponseReportView = createTeachingCallResponseReportView(workgroupId, year);
        return new TeachingCallResponseReportExcelView(teachingCallResponseReportView);
    }
}
