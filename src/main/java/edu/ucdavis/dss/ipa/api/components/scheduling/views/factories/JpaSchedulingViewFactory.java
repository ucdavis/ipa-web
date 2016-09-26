package edu.ucdavis.dss.ipa.api.components.scheduling.views.factories;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingViewSectionGroup;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaSchedulingViewFactory implements SchedulingViewFactory {
	@Inject SectionGroupService sectionGroupService;
	@Inject ActivityService activityService;
	@Inject CourseService courseService;
	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject UserRoleService userRoleService;
	@Inject LocationService locationService;
	@Inject TagService tagService;

	@Override
	public SchedulingView createSchedulingView(long workgroupId, long year, String termCode, Boolean showDoNotPrint) {
		List<Tag> tags = tagService.findByWorkgroupId(workgroupId);
		List<Location> locations = locationService.findByWorkgroupId(workgroupId);

		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<SectionGroup> sectionGroups;
		List<Course> courses;
		List<Activity> activities;
		if (showDoNotPrint != null && showDoNotPrint) {
			sectionGroups = sectionGroupService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
			courses = courseService.findByWorkgroupIdAndYear(workgroupId, year);
			activities = activityService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		} else {
			sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
			courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
			activities = activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		}

		return new SchedulingView(courses, sectionGroups, tags, locations, instructors, activities);
	}

	@Override
	public SchedulingViewSectionGroup createSchedulingViewSectionGroup(SectionGroup sectionGroup) {
		boolean IS_SHARED = true;
		boolean IS_NOT_SHARED = false;
		List<Activity> sharedActivities = activityService.findBySectionGroupId(sectionGroup.getId(), IS_SHARED);
		List<Activity> unSharedActivities = activityService.findBySectionGroupId(sectionGroup.getId(), IS_NOT_SHARED);
		List<TeachingCallResponse> teachingCallResponses = teachingCallResponseService.findBySectionGroup(sectionGroup);
		return new SchedulingViewSectionGroup(sectionGroup, sharedActivities, unSharedActivities, teachingCallResponses);
	}

	@Override
	public List<SchedulingViewSectionGroup> createSchedulingViewAllSectionGroups(long workgroupId, long year, String termCode, Boolean showDoNotPrint) {
		List<SchedulingViewSectionGroup> schedulingViewSectionGroups = new ArrayList<>();

		List<SectionGroup> sectionGroups;
		if (showDoNotPrint != null && showDoNotPrint) {
			sectionGroups = sectionGroupService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		} else {
			sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		}

		for (SectionGroup sectionGroup: sectionGroups) {
			schedulingViewSectionGroups.add(this.createSchedulingViewSectionGroup(sectionGroup));
		}

		return schedulingViewSectionGroups;
	}

}
