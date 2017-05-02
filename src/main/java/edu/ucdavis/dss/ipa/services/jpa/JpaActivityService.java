package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.ActivityRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class JpaActivityService implements ActivityService {
	@Inject ActivityRepository activityRepository;
	@Inject SectionService sectionService;

	@Override
	@Transactional
	public Activity saveActivity(Activity activity)
	{
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

			String dayIndicator = activity.getDayIndicator();
			Time startTime = activity.getStartTime();
			Time endTime = activity.getEndTime();
			char typeCode = activity.getActivityTypeCode().getActivityTypeCode();

			for (DwActivity dwActivity : dwSection.getActivities()) {
				String dwDayIndicator = dwActivity.getDay_indicator();
				String dwStartTime = dwActivity.getSsrmeet_begin_time();
				String dwEndTime = dwActivity.getSsrmeet_end_time();
				char dwTypeCode = dwActivity.getSsrmeet_schd_code();

				if (dayIndicator.equals(dwDayIndicator) && startTime.equals(dwStartTime) && endTime.equals(dwEndTime) && typeCode == dwTypeCode) {
					String bannerLocation = dwActivity.getSsrmeet_bldg_code() + " " + dwActivity.getSsrmeet_room_code();
					activity.setBannerLocation(bannerLocation);
					this.saveActivity(activity);
				}
			}
		}
	}

}
