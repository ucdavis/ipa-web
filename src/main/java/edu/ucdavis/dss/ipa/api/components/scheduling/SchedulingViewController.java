package edu.ucdavis.dss.ipa.api.components.scheduling;

import edu.ucdavis.dss.ipa.api.components.scheduling.views.SchedulingView;
import edu.ucdavis.dss.ipa.api.components.scheduling.views.factories.SchedulingViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.LocationService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class SchedulingViewController {

	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;
	@Inject ActivityService activityService;
	@Inject LocationService locationService;
	@Inject SchedulingViewFactory schedulingViewFactory;
	@Inject Authorizer authorizer;

	/**
	 * Delivers the JSON payload for the Scheduling View (nee Activities View), used on page load.
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	@RequestMapping(value = "/api/schedulingView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public SchedulingView showSchedulingView(@PathVariable long workgroupId,
											 @PathVariable long year,
											 @PathVariable String termCode) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return schedulingViewFactory.createSchedulingView(workgroupId, year, termCode);
	}

    @RequestMapping(value = "/api/schedulingView/activities/{activityId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Activity updateActivity(@PathVariable long activityId, @RequestBody Activity activity, HttpServletResponse httpResponse) {
    	Activity originalActivity = activityService.findOneById(activityId);


			if (originalActivity == null) {
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			SectionGroup sectionGroup = sectionGroupService.getOneById(originalActivity.getSectionGroupIdentification());
			Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
			authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

			originalActivity.setLocation(activity.getLocation());
			originalActivity.setActivityState(activity.getActivityState());
			originalActivity.setFrequency(activity.getFrequency());
			originalActivity.setDayIndicator(activity.getDayIndicator());
			originalActivity.setStartTime(activity.getStartTime());
			originalActivity.setEndTime(activity.getEndTime());

			return this.activityService.saveActivity(originalActivity);
    }

	@RequestMapping(value = "/api/schedulingView/activities/{activityId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteActivity(@PathVariable long activityId, HttpServletResponse httpResponse) {
		Activity activity = activityService.findOneById(activityId);
		if (activity == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		SectionGroup sectionGroup = sectionGroupService.getOneById(activity.getSectionGroupIdentification());
		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		this.activityService.deleteActivityById(activity.getId());
	}

	@RequestMapping(value = "/api/schedulingView/sectionGroups/{sectionGroupId}/activities/{activityCode}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Activity createSharedActivity(@PathVariable char activityCode, @PathVariable Long sectionGroupId, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		authorizer.hasWorkgroupRole(sectionGroup.getCourse().getSchedule().getWorkgroup().getId(), "academicPlanner");

		ActivityType activityType = new ActivityType(activityCode);
		Activity slotActivity = new Activity();
		slotActivity.setActivityTypeCode(activityType);
		slotActivity.setActivityState(ActivityState.DRAFT);
		slotActivity.setDayIndicator("0000000");
		slotActivity.setSectionGroup(sectionGroup);

		slotActivity = activityService.saveActivity(slotActivity);

		return slotActivity;
	}

	@RequestMapping(value = "/api/schedulingView/sections/{sectionId}/activities/{activityCode}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Activity createActivity(@PathVariable char activityCode, @PathVariable Long sectionId, HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(sectionId);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		authorizer.hasWorkgroupRole(section.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId(), "academicPlanner");

		ActivityType activityType = new ActivityType(activityCode);
		Activity newActivity = new Activity();
		newActivity.setActivityTypeCode(activityType);
		newActivity.setActivityState(ActivityState.DRAFT);
		newActivity.setDayIndicator("0000000");
		newActivity.setSection(section);

		newActivity = activityService.saveActivity(newActivity);

		return newActivity;
	}

	@RequestMapping(value = "/api/schedulingView/sections/{sectionId}/activities", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Activity> getActivitiesBySection(@PathVariable Long sectionId, HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(sectionId);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		authorizer.hasWorkgroupRole(section.getSectionGroup().getCourse().getSchedule().getWorkgroup().getId(), "academicPlanner");

		List<Activity> activities = new ArrayList<>();
		activities.addAll(section.getActivities());
		activities.addAll(section.getSectionGroup().getActivities());

		return activities;
	}
}
