package edu.ucdavis.dss.ipa.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;

@Validated
public interface DwScheduleService {
	/**
	 * Adds or updates a DwSectionGroup to the schedule with ID 'scheduleId'
	 * 
	 * @param dwCo
	 * @param scheduleId
	 */
	@Transactional
	void addOrUpdateDwSectionGroupToSchedule(DwSectionGroup dwCo, Schedule schedule, boolean markPublished);

	/**
	 * Uses the given DwSectionGroup to update the census snapshots found in a Schedule
	 * with ID scheduleId.
	 * 
	 * @param dwSg
	 * @param id
	 */
	@Transactional
	void updateCensusSnapshotsForSectionGroupAndSchedule(DwSectionGroup dwSg, long scheduleId);
}
