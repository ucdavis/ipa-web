package edu.ucdavis.dss.ipa.api.components.scheduling.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSchedulingViewFactory implements SchedulingViewFactory {
	@Inject SectionGroupService sectionGroupService;
	@Inject WorkgroupService workgroupService;
	@Inject CourseService courseService;

	@Override
	public SchedulingView createSchedulingView(long workgroupId, long year, String termCode, Boolean showDoNotPrint) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		if(workgroup == null) { return null; }

		List<SectionGroup> sectionGroups;
		List<Course> courses;
		if (showDoNotPrint != null && showDoNotPrint) {
			sectionGroups = sectionGroupService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
			courses = courseService.findByWorkgroupIdAndYear(workgroupId, year);
		} else {
			sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
			courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		}

		return new SchedulingView(courses, sectionGroups, workgroup.getTags());
	}

}
