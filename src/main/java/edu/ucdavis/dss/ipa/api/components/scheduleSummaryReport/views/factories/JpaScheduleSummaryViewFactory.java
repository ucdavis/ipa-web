package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class JpaScheduleSummaryViewFactory implements ScheduleSummaryViewFactory {
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject SectionService sectionService;
    @Inject ActivityService activityService;
    @Inject UserRoleService userRoleService;
    @Inject SupportAssignmentService supportAssignmentService;
    @Inject SupportStaffService supportStaffService;
    @Inject TermService termService;
    @Inject InstructorTypeService instructorTypeService;
    @Inject InstructorService instructorService;

    @Override
    public ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year, String shortTermCode) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<Section> sections = sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<Activity> activities = activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();

        Set<Instructor> instructors = new HashSet<Instructor>();
        Set<Instructor> activeInstructors = new HashSet<>(userRoleService.getInstructorsByWorkgroupId(schedule.getId(), workgroupId));
        Set<Instructor> assignedInstructors = new HashSet<> (instructorService.findAssignedByScheduleId(schedule.getId()));
        instructors.addAll(activeInstructors);
        instructors.addAll(assignedInstructors);

        List<SupportAssignment> supportAssignments = supportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<SupportStaff> supportStaffList = supportStaffService.findBySupportAssignments(supportAssignments);
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();
        return new ScheduleSummaryReportView(courses, sectionGroups, sections, activities, teachingAssignments, instructors, shortTermCode, year, supportAssignments, supportStaffList, instructorTypes);
    }

    @Override
    public View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode) {
        ScheduleSummaryReportView scheduleSummaryReportView = createScheduleSummaryReportView(workgroupId, year, termCode);
        return new ScheduleSummaryReportExcelView(scheduleSummaryReportView);
    }

}
