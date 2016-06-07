package edu.ucdavis.dss.ipa.fixes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FixSharedActivities {
	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Designed for one-time use.
	 * 
	 * Scans all activities looking for identical activities that exist on every section within a sectionGroup
	 * If found, set these activities shared flag to true
	 * 
	 * @param rootContext
	 */
	// Do not uncomment. This fix was applied March 10, 2016 and is kept for historical purposes.
	/*
	public static void fixSharedActivities(List<SectionGroup> sectionGroups, ApplicationContext rootContext) {
		ActivityService activityService = (ActivityService)rootContext.getBean(ActivityService.class);
		if(sectionGroups == null || sectionGroups.size() <= 0) {
			log.error("Unable to read sectionGroup data!");
			return;
		}

		// For each sectionGroup ...
		for (SectionGroup sectionGroup : sectionGroups) {
			List<Activity> potentialSharedActivities = new ArrayList<Activity>();
			boolean isFirstSectionInSectionGroup = true;

			// Determine which activities show up in every section
			for (Section section : sectionGroup.getSections()) {
				// Use activities from first section as a starting reference
				if (isFirstSectionInSectionGroup) {
					for (Activity activity : section.getActivities()) {
						potentialSharedActivities.add(activity);
					}
					isFirstSectionInSectionGroup = false;
				} else {
					// Verify each potentialSharedActivity is present in this section, remove it if not
					for (int i = potentialSharedActivities.size()-1; i >= 0; i--) {
						Activity potentialSharedActivity = potentialSharedActivities.get(i);
						boolean foundInSection = false;

						for (Activity activity : section.getActivities()) {
							if (activity.isDuplicate(potentialSharedActivity)) {
								foundInSection = true;
							}
						}

						if (foundInSection == false) {
							potentialSharedActivities.remove(i);
						}
					}
				}
			}
			// Set shared to true of relevant activities
			for (Activity sharedActivity : potentialSharedActivities) {
				for (Section section : sectionGroup.getSections()) {
					for (Activity activity : section.getActivities()) {
						if (activity.isDuplicate(sharedActivity) || activity.getId() == sharedActivity.getId()) {
							activity.setShared(true);
							activityService.saveActivity(activity);
						}
					}
				}
			}
		}
	}
	*/
}