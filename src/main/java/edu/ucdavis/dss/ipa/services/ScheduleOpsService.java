package edu.ucdavis.dss.ipa.services;

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
	 * Creates a new schedule by copying an existing one, as opposed to importing it from Data Warehouse.
	 * 
	 * @param workgroupId the ID of the workgroup who will own the schedule
	 * @param scheduleYear the year the new schedule should belong to
	 * @param copyFromYear the year of the schedule to copy. Must belong to the same workgroup.
	 * @param copyInstructors set to true if you want to copy instructors
	 * @param copyRooms set to true if you want to copy rooms
	 * @param copyTimes set to true if you want to copy activity times
	 * @return
	 */
	Schedule createScheduleFromExisting(Long workgroupId, Long scheduleYear, Long copyFromYear, Boolean copyInstructors, Boolean copyRooms, Boolean copyTimes);

	/**
	 * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
	 */
	void updateSectionsFromDW();

	void updateEmptySectionGroups();
}
