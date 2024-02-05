package edu.ucdavis.dss.ipa.services;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Activity;

@Validated
public interface ActivityService {

	Activity saveActivity(@NotNull @Valid Activity activity);

	Activity findOneById(Long id);
	
	void deleteActivityById(Long id);

	/**
	 * Deletes all activities for the given section
	 * 
	 * @param sectionId ID of the section whose activities will be deleted
	 */
	void deleteAllBySectionId(long sectionId);

	/**
	 * Returns 'Shared' or 'Unshared' for a given sectionGroup. Used in Scheduling View Factory
	 * @param sectionGroupId
	 * @param isShared
	 * @return
	 */
	List<Activity> findBySectionGroupId(long sectionGroupId, boolean isShared);

	List<Activity> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	List<Activity> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode);

	/**
	 * Looks for matching activities and will sync location data from the DwSection.
	 * Respects the syncLocation flag on Activities.
	 * @param dwSection
	 * @param activities
     */
	void syncActivityLocations(DwSection dwSection, List<Activity> activities);

	Activity createFromDwActivity(DwActivity dwActivity);
}
