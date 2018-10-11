package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Workgroup;

/**
 * Provides methods for importing, creating, or otherwise performing complex operations
 * on a schedule. For simple saving, deleting, etc., see ScheduleService.
 *
 */
@Validated
public interface ScheduleOpsService {
	/**
	 * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
	 */
	void updateSectionsByCourseFromDW(Course course);

	void updateEmptySectionGroups();
}
