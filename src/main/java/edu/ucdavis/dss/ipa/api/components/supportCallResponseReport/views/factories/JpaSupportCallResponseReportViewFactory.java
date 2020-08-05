package edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.factories;

import static edu.ucdavis.dss.ipa.api.helpers.Utilities.termToTermCode;

import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.SupportCallResponseReportExcelView;
import edu.ucdavis.dss.ipa.api.components.supportCallResponseReport.views.SupportCallResponseReportView;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
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
    @Inject StudentSupportCallResponseService studentSupportCallResponseService;
    @Inject StudentSupportPreferenceService studentSupportPreferenceService;
    @Inject UserRoleService userRoleService;

    @Override
    public SupportCallResponseReportView createSupportCallResponseReportView(long workgroupId,
                                                                             long year,
                                                                             String termShortCode) {
        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        if (schedule == null) {
            return null;
        }

        String termCode = Term.getTermCodeByYearAndTermCode(year, termShortCode);

        List<Course> courses = schedule.getCourses();
        List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
        List<StudentSupportCallResponse> studentSupportCallResponses = studentSupportCallResponseService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<StudentSupportPreference> studentSupportPreferences = studentSupportPreferenceService.findByScheduleIdAndTermCode(schedule.getId(), termCode);
        List<SupportStaff> studentStaff = userRoleService.findActiveSupportStaffByWorkgroupIdAndPreferences(workgroupId, studentSupportPreferences);

        return new SupportCallResponseReportView(courses, sectionGroups, studentSupportCallResponses, studentSupportPreferences, studentStaff, schedule, termCode);
    }

    @Override
    public View createSupportCallResponseReportExcelView(long workgroupId, long year, String termShortCode) {
        SupportCallResponseReportView supportCallResponseReportView = createSupportCallResponseReportView(workgroupId, year, termShortCode);
        return new SupportCallResponseReportExcelView(supportCallResponseReportView);
    }
}
