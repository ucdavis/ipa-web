package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaScheduleSummaryViewFactory implements ScheduleSummaryViewFactory {

    @Inject ScheduleService scheduleService;

    @Inject CourseService courseService;
    @Inject SectionGroupService sectionGroupService;
    @Inject SectionService sectionService;
    @Inject ActivityService activityService;

    @Inject TeachingAssignmentService teachingAssignmentService;
    @Inject InstructorService instructorService;
    @Inject UserRoleService userRoleService;

    @Override
    public ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year, String shortTermCode) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<Activity> activities = activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();
        List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);

        return new ScheduleSummaryReportView(courses, sectionGroups, sections, activities, teachingAssignments, instructors, shortTermCode, year);
    }

    @Override
    public View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode) {
        ScheduleSummaryReportView scheduleSummaryReportView = createScheduleSummaryReportView(workgroupId, year, termCode);
        return new ScheduleSummaryReportExcelView(scheduleSummaryReportView);
    }

}
