package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

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

	List<Schedule> findByWorkgroupId(long workgroupId);

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
	List<Term> getActiveTermCodesForSchedule(Schedule schedule);
}
