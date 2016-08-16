package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.ActivityRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.SectionService;

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
		Activity activity = this.findOneById(id);
		Section section = activity.getSection();
		section.getActivities().remove(activity);
		sectionService.save(section);
		activity = this.findOneById(id);
		activityRepository.delete(activity);
	}

	@Override
	public List<Activity> findSharedActivitySet(Long id) {
		// Specifies the codes that are considered duplicates

		Activity activity = this.findOneById(id);
		
		List<Activity> duplicateActivities = new ArrayList<Activity>();
		
		SectionGroup sectionGroup = activity.getSection().getSectionGroup();

		// Look through other activities within the same SectionGroup
		for(Section section : sectionGroup.getSections()) {
			for(Activity slotActivity : section.getActivities() ) {
				if (slotActivity.isShared() && slotActivity.getActivityTypeCode().getActivityTypeCode() == activity.getActivityTypeCode().getActivityTypeCode()) {
					duplicateActivities.add(slotActivity);
				}
			}
		}

		return duplicateActivities;
	}

	@Override
	public void deleteAllBySectionId(long sectionId) {
		this.activityRepository.deleteAllBySectionId(sectionId);
	}

	@Override
	public List<Activity> findBySectionGroupId(long sectionGroupId) {
		return activityRepository.findBySection_SectionGroup_Id(sectionGroupId);
	}
}
