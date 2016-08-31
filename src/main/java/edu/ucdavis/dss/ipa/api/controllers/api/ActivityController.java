package edu.ucdavis.dss.ipa.api.controllers.api;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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