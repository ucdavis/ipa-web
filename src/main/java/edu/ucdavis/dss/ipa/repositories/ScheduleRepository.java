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

	@Query("SELECT DISTINCT co.termCode FROM CourseOffering co, CourseOfferingGroup cog WHERE co.courseOfferingGroup = cog and cog.schedule.id = :scheduleId")
	List<String> getActiveTermCodesForScheduleId(@Param("scheduleId") long scheduleId);
}
