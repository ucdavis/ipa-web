package edu.ucdavis.dss.ipa.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;

import java.util.List;

@Validated
public interface DwScheduleService {
	/**
	 * Adds or updates a DwSectionGroup to the schedule with ID 'scheduleId'
	 * 
	 * @param dwCo
	 * @param schedule
	 */
	@Transactional
	void addOrUpdateDwSectionGroupToSchedule(DwSectionGroup dwCo, Schedule schedule, boolean markPublished);

	List<DwSectionGroup> getSectionGroupsByCourseIdAndTermCode(long courseId, String termCode);

}
