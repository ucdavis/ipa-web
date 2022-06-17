package edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.factories;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportAnnualExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportExcelView;
import edu.ucdavis.dss.ipa.api.components.scheduleSummaryReport.views.ScheduleSummaryReportView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.TermDescription;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
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
    @Inject DataWarehouseService dwService;

    @Override
    public ScheduleSummaryReportView createScheduleSummaryReportView(long workgroupId, long year, String shortTermCode,
                                                                     boolean simpleView) {
        Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups =
            sectionGroupService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<Section> sections =
            sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<Activity> activities =
            activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, shortTermCode);
        List<TeachingAssignment> teachingAssignments = schedule.getTeachingAssignments();

        Set<Instructor> instructors = new HashSet<Instructor>();
        Set<Instructor> activeInstructors =
            new HashSet<>(userRoleService.getInstructorsByScheduleIdAndWorkgroupId(schedule.getId(), workgroupId));
        Set<Instructor> assignedInstructors =
            new HashSet<>(instructorService.findAssignedByScheduleId(schedule.getId()));
        instructors.addAll(activeInstructors);
        instructors.addAll(assignedInstructors);

        List<SupportAssignment> supportAssignments =
            supportAssignmentService.findByScheduleIdAndTermCode(schedule.getId(), shortTermCode);
        List<SupportStaff> supportStaffList = supportStaffService.findBySupportAssignments(supportAssignments);
        List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

        Map<String, Map<String, Long>> termCodeCensusMap = null;
        Map<String, Map<String, Long>> courseCensusMap = dwService.generateCourseCensusMap(courses);

        return new ScheduleSummaryReportView(courses, sectionGroups, sections, activities, teachingAssignments,
            instructors, shortTermCode, year, supportAssignments, supportStaffList, instructorTypes, simpleView,
            termCodeCensusMap, courseCensusMap);
    }

    @Override
    public View createScheduleSummaryReportExcelView(long workgroupId, long year, String termCode, boolean simpleView) {
        ScheduleSummaryReportView scheduleSummaryReportView =
            createScheduleSummaryReportView(workgroupId, year, termCode, simpleView);
        return new ScheduleSummaryReportExcelView(scheduleSummaryReportView);
    }

    @Override
    public View createScheduleSummaryReportAnnualExcelView(long workgroupId, long year) {
        List<ScheduleSummaryReportView> scheduleSummaryReportViewList = new ArrayList<>();
        String[] academicYearTermCodes =
            {TermDescription.FALL.getTermCode(year), TermDescription.WINTER.getTermCode(year),
                TermDescription.SPRING.getTermCode(year)};

        for (String termCode : academicYearTermCodes) {
            scheduleSummaryReportViewList.add(createScheduleSummaryReportView(workgroupId, year, termCode, false));
        }

        return new ScheduleSummaryReportAnnualExcelView(scheduleSummaryReportViewList);
    }
}
