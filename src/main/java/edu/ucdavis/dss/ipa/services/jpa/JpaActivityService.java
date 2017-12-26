package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.repositories.ActivityRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

@Service
public class JpaActivityService implements ActivityService {
	@Inject ActivityRepository activityRepository;

	@Override
	@Transactional
	public Activity saveActivity(Activity activity) {

		Activity originalActivity = this.findOneById(activity.getId());

		Long originalLocationId = null;
		if (originalActivity != null && originalActivity.getLocation() != null) {
				originalLocationId = originalActivity.getLocation().getId();
		}

		Long newlocationId = null;
		if (activity.getLocation() != null) {
			newlocationId = activity.getLocation().getId();
		}

		// Case 1: Activity is saved and location is changed by user
		if (newlocationId != null && (originalLocationId == null || newlocationId != originalLocationId)) {
			activity.setBannerLocation(null);
			activity.setSyncLocation(true);
		}

		// Case 2: BannerLocation is set via sync or diff view
		if (activity.getBannerLocation() != null && activity.getBannerLocation().length() > 0) {
			activity.setSyncLocation(true);
			activity.setLocation(null);
		}

		if (activity.getDayIndicator() == null) {
			// Ensure default dayIndicator pattern is set
			activity.setDayIndicator("0000000");
		}

		// Activity frequency should be a minimum of once a week
		if (activity.getFrequency() < 1) {
			activity.setFrequency(1);
		}

		return this.activityRepository.save(activity);
	}


	@Override
	public Activity findOneById(Long id) {
		return this.activityRepository.findOne(id);
	}

	@Override
	@Transactional
	public void deleteActivityById(Long id) {
		activityRepository.delete(id);
	}

	@Override
	public void deleteAllBySectionId(long sectionId) {
		this.activityRepository.deleteAllBySectionId(sectionId);
	}

	@Override
	public List<Activity> findBySectionGroupId(long sectionGroupId, boolean isShared) {
		if (isShared) {
			return activityRepository.findBySectionGroupId(sectionGroupId);
		} else {
			return activityRepository.findBySection_SectionGroup_Id(sectionGroupId);
		}
	}

	@Override
	public List<Activity> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return activityRepository.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public List<Activity> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return activityRepository.findVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public void syncActivityLocations(DwSection dwSection, List<Activity> activities) {
		for (Activity activity : activities) {
			if (activity.isSyncLocation() == false) {
				continue;
			}

			String dayIndicator = "";
			if (activity.getDayIndicator() != null) {
				dayIndicator = activity.getDayIndicator().toString();
			}

			String startTime = "";
			if (activity.getStartTime() != null) {
				startTime = activity.getStartTime().toString();
			}

			String endTime = "";
			if (activity.getEndTime() != null) {
				endTime = activity.getEndTime().toString();
			}

			String typeCode = String.valueOf(activity.getActivityTypeCode().getActivityTypeCode());

			for (DwActivity dwActivity : dwSection.getActivities()) {
				String dwDayIndicator = dwActivity.getDay_indicator();
				String dwStartTime = dwActivity.getSsrmeet_begin_time();
				String dwEndTime = dwActivity.getSsrmeet_end_time();
				String dwTypeCode = String.valueOf(dwActivity.getSsrmeet_schd_code());

				// Ensure DW location data is valid
				if (dwActivity.getSsrmeet_bldg_code() == null || dwActivity.getSsrmeet_bldg_code().length() > 0
				|| dwActivity.getSsrmeet_room_code() == null || dwActivity.getSsrmeet_room_code().length() > 0) {
					continue;
				}

				// Ensure DW data is referring to the same activity
				if (dayIndicator.equals(dwDayIndicator)
				&& startTime.equals(dwStartTime)
				&& endTime.equals(dwEndTime)
				&& typeCode.equals(dwTypeCode) ) {
					// Update location data
					String bannerLocation = dwActivity.getSsrmeet_bldg_code() + " " + dwActivity.getSsrmeet_room_code();
					activity.setBannerLocation(bannerLocation);
					this.saveActivity(activity);
				}
			}
		}
	}

	@Override
	public Activity createFromDwActivity(DwActivity dwActivity) {
		Activity activity = new Activity();

		ActivityType activityType = new ActivityType();
		activityType.setActivityTypeCode(dwActivity.getSsrmeet_schd_code());

		activity.setActivityTypeCode(activityType);

		String rawStartTime = dwActivity.getSsrmeet_begin_time();

		if (rawStartTime != null) {
			String minutes = rawStartTime.substring(2, 4);
			String hours = rawStartTime.substring(0, 2);
			String formattedStartTime = hours + ":" + minutes + ":00";
			Time startTime = java.sql.Time.valueOf(formattedStartTime);

			activity.setStartTime(startTime);
		}

		String rawEndTime = dwActivity.getSsrmeet_end_time();

		if (rawEndTime != null) {
			String minutes = rawStartTime.substring(2, 4);
			String hours = rawStartTime.substring(0, 2);
			String formattedEndTime = hours + ":" + minutes + ":00";
			Time endTime = java.sql.Time.valueOf(formattedEndTime);

			activity.setEndTime(endTime);
		}

		String dayIndicator = dwActivity.getDay_indicator();
		activity.setDayIndicator(dayIndicator);

		activity.setActivityState(ActivityState.DRAFT);

		return activity;
	}
}
