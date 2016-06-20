package edu.ucdavis.dss.ipa.repositories;


import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.Schedule;

public interface ScheduleRepository extends CrudRepository<Schedule, Long> {
	Schedule findOneByYear(long year);

	@Query("FROM Schedule s WHERE s.workgroup.id = :workgroupId AND s.year = :year")
	Schedule findOneByYearAndWorkgroupWorkgroupId(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year);

	Schedule findOneByWorkgroupCodeAndYear(String workgroupCode, long year);

	List<Schedule> findByYear(long year);

	@Query("SELECT DISTINCT sg.termCode FROM SectionGroup sg, Course c WHERE sg.course = c and c.schedule.id = :scheduleId")
	List<String> getActiveTermCodesForScheduleId(@Param("scheduleId") long scheduleId);
}
