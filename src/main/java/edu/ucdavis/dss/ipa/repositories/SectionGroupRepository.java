package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SectionGroupRepository extends CrudRepository<SectionGroup, Long> {

	public SectionGroup findById(Long id);

	List<SectionGroup> findByCourseScheduleWorkgroupIdAndCourseScheduleYear(long workgroupId, long year);

	/**
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	@Query( " SELECT DISTINCT sg" +
			" FROM SectionGroup sg, Course c, Schedule sch" +
			" WHERE sg.course = c" +
			" AND c.schedule = sch" +
			" AND sch.year = :year " +
			" AND sch.workgroup.id = :workgroupId " +
			" AND sg.termCode = :termCode " +
			" AND sg.id NOT IN (SELECT sg.id FROM SectionGroup sg, Section s WHERE s.sectionGroup = sg)")
	List<SectionGroup> findChildlessByWorkgroupIdAndYearAndTermCode(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year,
			@Param("termCode") String termCode);

	@Query( " SELECT DISTINCT sg" +
			" FROM Course c, SectionGroup sg, Section s, Schedule sch" +
			" WHERE sg.course = c" +
			" AND s.sectionGroup = sg" +
			" AND c.schedule = sch" +
			" AND sch.year = :year" +
			" AND sch.workgroup.id = :workgroupId" +
			" AND sg.termCode = :termCode" +
			" AND (s.visible = true OR s.visible IS NULL)")
	List<SectionGroup> findVisibleByWorkgroupIdAndYearAndTermCode(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year,
			@Param("termCode") String termCode);
}
