package edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportExcelView;
import edu.ucdavis.dss.ipa.api.components.teachingCallResponseReport.views.TeachingCallResponseReportView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

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

        List<Instructor> instructors = Stream.concat(
                teachingCallReceipts.stream().map(TeachingCallReceipt::getInstructor),
                teachingAssignments.stream().map(TeachingAssignment::getInstructor).filter(Objects::nonNull))
            .distinct().collect(Collectors.toList());

        return new TeachingCallResponseReportView(courses, sectionGroups, teachingAssignments, teachingCallReceipts, teachingCallResponses, instructors, schedule);
    }

    @Override
    public View createTeachingCallResponseReportExcelView(long workgroupId, long year) {
        TeachingCallResponseReportView teachingCallResponseReportView = createTeachingCallResponseReportView(workgroupId, year);
        return new TeachingCallResponseReportExcelView(teachingCallResponseReportView);
    }
}
