package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.api.components.term.views.TermActivityView;

@RestController
public class ActivityController {
	@Inject ActivityService activityService;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;

	@PreAuthorize("hasPermission(#id, 'activity', 'academicCoordinator')")
	@RequestMapping(value = "/api/activities/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteActivity(@PathVariable Long id, HttpServletResponse httpResponse)
	{
		List<Activity> activitiesToDelete = new ArrayList<Activity>();
		Activity activity = activityService.findOneById(id);

		if (activity.isShared()) {
			activitiesToDelete = this.activityService.findSharedActivitySet(id);
		} else {
			activitiesToDelete.add(activity);
		}

		for(Activity slotActivity : activitiesToDelete) {
			this.activityService.deleteActivityById(slotActivity.getId());
		}

		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}

	@RequestMapping(value = "/api/sections/{id}/activities", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPermission(#id, 'section', 'academicCoordinator')")
	public TermActivityView createUniqueActivity(@RequestBody Activity activity, @PathVariable Long id, HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(id);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.METHOD_NOT_ALLOWED.value());
			return null;
		} else {
			activity.setSection(section);
			activity.setShared(false);

			Activity newActivity = activityService.saveActivity(activity);
			List<Activity> activities = section.getActivities();
			activities.add(newActivity);
			section.setActivities(activities);
			sectionService.save(section);

			httpResponse.setStatus(HttpStatus.OK.value());
			return new TermActivityView(newActivity);
		}
	}

	@PreAuthorize("hasPermission(#id, 'activity', 'academicCoordinator')")
	@RequestMapping(value = "/api/activities/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public Activity updateActivity( @RequestBody Activity activity, @PathVariable Long id, HttpServletResponse httpResponse_p) {
		Activity activityToReturn = null;
		List<Activity> activitiesToChange = new ArrayList<Activity>();
		Activity originalActivity = activityService.findOneById(id);

		if (activity.isShared()) {
			activitiesToChange = this.activityService.findSharedActivitySet(id);
		} else {
			activitiesToChange.add(activity);
		}

		for(Activity activityToChange : activitiesToChange) {
			activityToChange.setActivityTypeCode(activity.getActivityTypeCode());
			activityToChange.setBeginDate(activity.getBeginDate());
			activityToChange.setEndDate(activity.getEndDate());
			activityToChange.setBannerLocation(activity.getBannerLocation());
			activityToChange.setActivityState(activity.getActivityState());
			activityToChange.setDayIndicator(activity.getDayIndicator());
			activityToChange.setEndTime(activity.getEndTime());
			activityToChange.setStartTime(activity.getStartTime());
			activityToChange.setVirtual(activity.isVirtual());
			activityToChange.setSection(originalActivity.getSection());

			if (activityToChange.getId() == id) {
				activityToReturn = activityToChange;
			}

			this.activityService.saveActivity(activityToChange);
		}

		httpResponse_p.setStatus(HttpStatus.OK.value());
		
		return activityToReturn;
	}
}