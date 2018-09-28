package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.repositories.ActivityRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
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

		// If activity is numeric, then it should always be tied to a sectionGroup and not a section
		if (activity.getSection() != null && Utilities.isNumeric(activity.getSection().getSequenceNumber()) == true) {
			activity.setSectionGroup(activity.getSection().getSectionGroup());
			activity.setSection(null);
		}

		// If activity matches an existing shared activity, do nothing
		if(activity.getSection() != null && activity.getSection().getSectionGroup() != null) {
			if (this.matchesSharedActivity(activity, activity.getSection().getSectionGroup())) {
				return null;
			}
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
			// Only sync locations when allowed (i.e. not custom space)
			boolean shouldSyncLocation = true;

			if (activity.isSyncLocation() == false) {
				shouldSyncLocation = false;
			}

			String dayIndicator = "";
			if (activity.getDayIndicator() != null) {
				dayIndicator = activity.getDayIndicator();
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

				boolean isDwLocationValid = true;

				// Ensure DW location data is valid
				if (dwActivity.getSsrmeet_bldg_code() == null || dwActivity.getSsrmeet_bldg_code().length() > 0
				|| dwActivity.getSsrmeet_room_code() == null || dwActivity.getSsrmeet_room_code().length() > 0) {
					isDwLocationValid = false;
				}

				// Ensure DW data is referring to the same activity
				if (dayIndicator.equals(dwDayIndicator)
				&& startTime.equals(dwStartTime)
				&& endTime.equals(dwEndTime)
				&& typeCode.equals(dwTypeCode) ) {
					// Update category data
					if (activity.getCategory() == null) {
						activity.setCategory(Long.valueOf(dwActivity.getCatagory()));
						System.out.println("set catagory value");
					}

					// Update location data
					if (shouldSyncLocation && isDwLocationValid) {
						String bannerLocation = dwActivity.getSsrmeet_bldg_code() + " " + dwActivity.getSsrmeet_room_code();
						activity.setBannerLocation(bannerLocation);
					}

					this.saveActivity(activity);
				}
			}
		}
	}

	@Override
	public Activity createFromDwActivity(DwActivity dwActivity) {
		Activity activity = new Activity();

		activity.setActivityTypeCode(dwActivity.getSsrmeet_schd_code());
		activity.setStartTime(dwActivity.castBeginTime());
		activity.setEndTime(dwActivity.castEndTime());
		activity.setDayIndicator(dwActivity.getDay_indicator());
		activity.setActivityState(ActivityState.DRAFT);

		return activity;
	}

	private boolean matchesSharedActivity(Activity activity, SectionGroup sectionGroup) {
		char typeCode = activity.getActivityTypeCode().getActivityTypeCode();
		String startTime = activity.getStartTime().toString() != null ? activity.getStartTime().toString() : "";
		String endTime = activity.getEndTime().toString() != null ? activity.getEndTime().toString() : "";
		String days = activity.getDayIndicator();

		// Activities with null days/times cannot be considered shared
		if (startTime == "" || endTime == "" || days.indexOf("1") == -1) {
			return false;
		}

		String activityKey = typeCode + startTime + endTime + days;

		for (Activity slotActivity : sectionGroup.getActivities()) {
			char slotTypeCode = slotActivity.getActivityTypeCode().getActivityTypeCode();
			String slotStartTime = slotActivity.getStartTime().toString() != null ? slotActivity.getStartTime().toString() : "";
			String slotEndTime = slotActivity.getEndTime().toString() != null ? slotActivity.getEndTime().toString() : "";
			String slotDays = slotActivity.getDayIndicator();
			String slotActivityKey = slotTypeCode + slotStartTime + slotEndTime + slotDays;

			if (slotActivityKey.equals(activityKey)) {
				return true;
			}
		}

		return false;
	}
}
