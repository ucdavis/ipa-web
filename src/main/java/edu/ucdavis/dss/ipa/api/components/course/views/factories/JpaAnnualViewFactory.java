package edu.ucdavis.dss.ipa.api.components.course.views.factories;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseExcelView;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaAnnualViewFactory implements AnnualViewFactory {
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;
	@Inject WorkgroupService workgroupService;
	@Inject CourseService courseService;
	@Inject TermService termService;

	@Override
	public CourseView createCourseView(long workgroupId, long year, Boolean showDoNotPrint) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		if(workgroup == null) { return null; }

		Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		List<ScheduleTermState> scheduleTermStates = scheduleTermStateService.getScheduleTermStatesBySchedule(schedule);
		List<SectionGroup> sectionGroups = sectionGroupService.findByWorkgroupIdAndYear(workgroupId, year);

		// TODO: make sure banner has terms after 2099, not urgent, just fix before 2099
		List<Term> terms = termService.findByYear(year);

		List<Course> courses;
		if (showDoNotPrint != null && showDoNotPrint) {
			courses = schedule.getCourses();
		} else {
			courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		}

		return new CourseView(courses, sectionGroups, workgroup.getTags(), terms);
	}

    @Override
    public View createAnnualScheduleExcelView(long workgroupId, long year, Boolean showDoNotPrint) {
    	CourseView courseView = createCourseView(workgroupId, year, showDoNotPrint);
		return new CourseExcelView(courseView);
    }

}
