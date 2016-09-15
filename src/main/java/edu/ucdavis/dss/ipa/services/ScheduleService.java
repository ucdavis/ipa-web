package edu.ucdavis.dss.ipa.services;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;

/**
 * Provides various operations to perform on schedules in the local database.
 * 
 */
@Validated
public interface ScheduleService {
	List<Schedule> findAll();
	
	Schedule saveSchedule(Schedule schedule);

	Schedule findById(long scheduleId);

	Schedule createSchedule(Long workgroupId, long year);

	Workgroup getWorkgroupByScheduleId(Long scheduleId);

	Schedule findByWorkgroupIdAndYear(long workgroupId, long year);

	Schedule findOrCreateByWorkgroupIdAndYear(long workgroupId, long year);

	List<User> getUserInstructorsByScheduleIdAndTermCode(Long scheduleId, String termCode);

	boolean deleteByScheduleId(long scheduleId);

	boolean isScheduleClosed(long scheduleId);

	/**
	 * Returns all term codes used by the given schedule.
	 * 
	 * @param schedule
	 * @return
	 */
	List<String> getActiveTermCodesForSchedule(Schedule schedule);
}
