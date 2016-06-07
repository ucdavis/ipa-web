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
	 * Imports schedules from DW for the given year(s) in a background task.
	 * 
	 * @param workgroup the workgroup to own the schedules found in DW
	 * @param startYear the start of a range of schedules we wish to import
	 * @param endYear the end of the range of schedules we wish to import
	 */
	@Async
	void importSchedulesFromDataWarehouse(Workgroup workgroup, long startYear, long endYear);
	
	/**
	 * Imports workgroup users from DW in a background task.
	 * 
	 * @param workgroup the workgroup to add users to
	 */
	@Async
	@Transactional
	void importWorkgroupUsersFromDataWarehouse(Workgroup workgroup);
}
