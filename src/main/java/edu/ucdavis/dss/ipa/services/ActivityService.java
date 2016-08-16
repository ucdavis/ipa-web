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
	 * Returns a set of all shared activities within the sectionGroup that hhave the same activityTypeCode.
	 * The supplied activity is included in the set.
	 */
	List<Activity> findSharedActivitySet(Long id);

	/**
	 * Deletes all activities for the given section
	 * 
	 * @param setionId ID of the section whose activities will be deleted
	 */
	void deleteAllBySectionId(long sectionId);

	List<Activity> findBySectionGroupId(long sectionGroupId);
}
