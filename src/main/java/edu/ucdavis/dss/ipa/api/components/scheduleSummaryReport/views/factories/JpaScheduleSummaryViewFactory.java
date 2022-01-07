package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportCourseListingsExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.SupportAssignmentService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

@Service
public class JpaScheduleSummaryViewFactory implements ScheduleSummaryViewFactory {
    @Inject
    ScheduleService scheduleService;
    @Inject
    SectionGroupService sectionGroupService;
    @Inject
    SectionService sectionService;
    @Inject
    ActivityService activityService;
    @Inject
    UserRoleService userRoleService;
    @Inject
    SupportAssignmentService supportAssignmentService;
    @Inject
    SupportStaffService supportStaffService;
    @Inject
    TermService termService;
    @Inject
    InstructorTypeService instructorTypeService;
    @Inject
    InstructorService instructorService;

    @Override
    public ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year,
                                                                     String shortTermCode,
                                                                     boolean simpleView) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups =
            sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<Section> sections =
            sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year,
                shortTermCode);
        List<Activity> activities =
            activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year,
                shortTermCode);
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();

        Set<Instructor> instructors = new HashSet<Instructor>();
        Set<Instructor> activeInstructors = new HashSet<>(
            userRoleService.getInstructorsByScheduleIdAndWorkgroupId(schedule.getId(),
                workgroupId));
        Set<Instructor> assignedInstructors =
            new HashSet<>(instructorService.findAssignedByScheduleId(schedule.getId()));
        instructors.addAll(activeInstructors);
        instructors.addAll(assignedInstructors);

        List<SupportAssignment> supportAssignments =
            supportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<SupportStaff> supportStaffList =
            supportStaffService.findBySupportAssignments(supportAssignments);
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        return new ScheduleSummaryReportView(courses, sectionGroups, sections, activities,
            teachingAssignments, instructors, shortTermCode, year, supportAssignments,
            supportStaffList, instructorTypes, simpleView);
    }

    @Override
    public View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode,
                                                     boolean simpleView) {
        ScheduleSummaryReportView scheduleSummaryReportView =
            createScheduleSummaryReportView(workgroupId, year, termCode, simpleView);
        return new ScheduleSummaryReportExcelView(scheduleSummaryReportView);
    }

    @Override
    public View createScheduleSummaryReportCourseListingsExcelView(long workgroupId, long year,
                                                                   String termCode) {
        List<Integer> workgroupIds = Arrays.asList(24,82,18,19,83,64,84,12,81,25,42,41,61,37,36,60,58,48,49,39,38,50,51,45,40,16,66,88,52,69,46,53,59,65,76,89,54,14,56,17,28,43,78,93,94,95,96,97,67,99,100);

        List<ScheduleSummaryReportView> reportViews = new ArrayList<>();

        for (Integer id : workgroupIds) {
            reportViews.add(createScheduleSummaryReportView(id, year, termCode, false));
        }

        return new ScheduleSummaryReportCourseListingsExcelView(reportViews);
    }

}
