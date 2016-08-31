package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
}
