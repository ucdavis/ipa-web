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
	@Inject SectionService sectionService;
	@Inject ActivityService activityService;
	@Inject CourseService courseService;
	@Inject TeachingCallResponseService teachingCallResponseService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject UserRoleService userRoleService;
	@Inject LocationService locationService;
	@Inject TagService tagService;
	@Inject TermService termService;
	@Inject InstructorTypeService instructorTypeService;

	@Override
	public SchedulingView createSchedulingView(long workgroupId, long year, String termCode) {
		List<Tag> tags = tagService.findByWorkgroupId(workgroupId);
		List<Location> locations = locationService.findByWorkgroupId(workgroupId);
		Term term = termService.getOneByTermCode(termCode);
		List<Instructor> instructors = userRoleService.getInstructorsByWorkgroupId(workgroupId);
		List<SectionGroup> sectionGroups = sectionGroupService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		List<Activity> activities = activityService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<Section> sections =sectionService.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<TeachingCallResponse> teachingCallResponses = teachingCallResponseService.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<TeachingAssignment> teachingAssignments = teachingAssignmentService.findApprovedByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<InstructorType> instructorTypes = instructorTypeService.getAllInstructorTypes();

		return new SchedulingView(courses, sectionGroups, tags, locations, instructors, activities, term, sections, teachingCallResponses, teachingAssignments, instructorTypes);
	}

}
