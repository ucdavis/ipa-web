package edu.ucdavis.dss.ipa.repositories;


import java.util.List;

import edu.ucdavis.dss.ipa.entities.Term;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.Schedule;

public interface ScheduleRepository extends CrudRepository<Schedule, Long> {
	@Query("FROM Schedule s WHERE s.workgroup.id = :workgroupId AND s.year = :year")
	Schedule findOneByYearAndWorkgroupWorkgroupId(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year);

	List<Schedule> findByYearGreaterThanEqual(long year);

	@Query("SELECT DISTINCT t " +
			" FROM SectionGroup sg, Course c, Term t " +
			" WHERE sg.course = c " +
			" AND sg.termCode = t.termCode" +
			" AND c.schedule.id = :scheduleId")
	List<Term> getActiveTermsForScheduleId(@Param("scheduleId") long scheduleId);
}
