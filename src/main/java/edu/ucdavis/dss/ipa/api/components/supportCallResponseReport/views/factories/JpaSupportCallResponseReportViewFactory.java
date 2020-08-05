package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.factories;

import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.SupportCallResponseReportExcelView;
import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.SupportCallResponseReportView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.StudentSupportPreferenceService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSupportCallResponseReportViewFactory implements SupportCallResponseReportViewFactory {
    @Inject ScheduleService scheduleService;
    @Inject SectionGroupService sectionGroupService;
    @Inject StudentSupportPreferenceService studentSupportPreferenceService;
    @Inject UserRoleService userRoleService;

    @Override
    public SupportCallResponseReportView createSupportCallResponseReportView(long workgroupId,
                                                                             long year) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        if (schedule == null) {
            return null;
        }

        // generate for year or just one term?
        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);

        List<StudentSupportCallResponse> studentSupportCallResponses = schedule.getStudentSupportCallResponses();
        List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleId(schedule.getId());

        // Find all support staff and combine them
        Set<SupportStaff> activeStaffList = new HashSet<>(userRoleService.findActiveSupportStaffByWorkgroupIdAndPreferences(workgroupId, studentSupportPreferences));
        Set<SupportStaff> referencedSupportStaff = new HashSet<> ();


        Set<SupportStaff> supportStaffSet = new HashSet<>();
        supportStaffSet.addAll(activeStaffList);
        supportStaffSet.addAll(referencedSupportStaff);

        return new SupportCallResponseReportView(courses, sectionGroups, studentSupportCallResponses, studentSupportPreferences, new ArrayList<>(supportStaffSet), schedule);
    }

    @Override
    public View createSupportCallResponseReportExcelView(long workgroupId, long year) {
        SupportCallResponseReportView supportCallResponseReportView = createSupportCallResponseReportView(workgroupId, year);
        return new SupportCallResponseReportExcelView(supportCallResponseReportView);
    }
}
