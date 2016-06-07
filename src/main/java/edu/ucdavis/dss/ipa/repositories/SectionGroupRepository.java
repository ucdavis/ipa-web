package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;

public interface SectionGroupRepository extends CrudRepository<SectionGroup, Long> {

	@Query("FROM SectionGroup sg WHERE sg.courseOffering.courseOfferingGroup.schedule.id = :scheduleId"
			+ " AND sg.courseOffering.courseOfferingGroup.course.id = :courseId"
			+ " AND sg.id IN (SELECT s.sectionGroup.id"
			+ " FROM Section s where s.sequenceNumber LIKE :sequencePattern ) ")
	public List<SectionGroup> findOneByScheduleIdAndCourseIdAndSequencePattern(
			@Param("scheduleId") long scheduleId, @Param("courseId") long courseId,
			@Param("sequencePattern") String sequencePattern);

	@Query("FROM SectionGroup sg WHERE sg.courseOffering.courseOfferingGroup.course.id = :courseId"
			+ " AND sg.id IN (SELECT s.sectionGroup.id"
			+ " FROM Section s where s.sequenceNumber LIKE :sequencePattern ) ")
	public List<SectionGroup> findByCourseIdAndSequencePattern(
			@Param("courseId") long courseId,
			@Param("sequencePattern") String sequencePattern);

	public List<SectionGroup> findByCourseOfferingIdIn(List<Long> courseOfferingGroupIds);

	public List<SectionGroup> findByCourseOfferingCourseOfferingGroupCourseAndCourseOfferingTermCodeInOrderByCourseOfferingTermCodeDesc(Course course, List<String> termCodes);
	
	@Query("FROM SectionGroup sg WHERE sg.courseOffering.courseOfferingGroup.course.subjectCode = :subjectCode"
			+ " AND sg.courseOffering.courseOfferingGroup.course.courseNumber = :courseNumber"
			+ " AND sg.courseOffering.courseOfferingGroup.schedule.id = :scheduleId"
			+ " AND sg.courseOffering.courseOfferingGroup.title = :title ")
	public List<SectionGroup> findBySubjectCodeAndCourseNumberAndScheduleIdAndTitle(
			@Param("subjectCode") String subjectCode,
			@Param("courseNumber") String courseNumber,
			@Param("scheduleId") long scheduleId,
			@Param("title") String title);

	public SectionGroup findById(Long id);

	public List<SectionGroup> findByCourseOfferingId(long id);

	@Query("SELECT sequenceNumber FROM Section s where s.sectionGroup.id = :sectionGroupId")
	public List<String> findSequenceSamplesBySectionGroupId(@Param("sectionGroupId") long sectionGroupId);
}
