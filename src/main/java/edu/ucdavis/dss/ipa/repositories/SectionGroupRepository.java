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
	 * Finds sectionGroups that have no sections
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
	List<SectionGroup> findEmptyByWorkgroupIdAndYearAndTermCode(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year,
			@Param("termCode") String termCode);

	/**
	 * Finds sectionGroups that do have sections and those sections have the
	 * visible flag set to true or Null
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	@Query( " SELECT DISTINCT sg" +
			" FROM Course c, SectionGroup sg, Section s, Schedule sch" +
			" WHERE sg.course = c" +
			" AND s.sectionGroup = sg" +
			" AND c.schedule = sch" +
			" AND sch.year = :year" +
			" AND sch.workgroup.id = :workgroupId" +
			" AND sg.termCode = :termCode" +
			" AND (s.visible = true OR s.visible IS NULL)")
	List<SectionGroup> findOccupiedVisibleByWorkgroupIdAndYearAndTermCode(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year,
			@Param("termCode") String termCode);

	/**
	 * Finds all sectionGroups for the given params no filtering, even the doNotPrint
	 * and empty ones
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	List<SectionGroup> findByCourseScheduleWorkgroupIdAndCourseScheduleYearAndTermCode(long workgroupId, long year, String termCode);

	/**
	 * Finds sectionGroups that are not empty (includes doNotPrint ones)
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	@Query( " SELECT DISTINCT sg" +
			" FROM Course c, SectionGroup sg, Section s, Schedule sch" +
			" WHERE sg.course = c" +
			" AND s.sectionGroup = sg" +
			" AND c.schedule = sch" +
			" AND sch.year = :year" +
			" AND sch.workgroup.id = :workgroupId" +
			" AND sg.termCode = :termCode")
	List<SectionGroup> findOccupiedByWorkgroupIdAndYearAndTermCode(
			@Param("workgroupId") long workgroupId,
			@Param("year") long year,
			@Param("termCode") String termCode);
}
