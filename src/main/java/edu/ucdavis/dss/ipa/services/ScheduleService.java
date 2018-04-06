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

	List<Schedule> findAllCurrentAndFuture();
	
	Schedule saveSchedule(Schedule schedule);

	Schedule findById(long scheduleId);

	Schedule createSchedule(Long workgroupId, long year);

	Workgroup getWorkgroupByScheduleId(Long scheduleId);

	Schedule findByWorkgroupIdAndYear(long workgroupId, long year);

	Schedule findOrCreateByWorkgroupIdAndYear(long workgroupId, long year);

	boolean isScheduleClosed(long scheduleId);

	/**
	 * Returns all term codes used by the given schedule.
	 * 
	 * @param schedule
	 * @return
	 */
	List<Term> getActiveTermCodesForSchedule(Schedule schedule);
}
