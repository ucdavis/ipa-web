package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseRepository extends CrudRepository<Course, Long> {

	Course findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(String subjectCode, String courseNumber, String sequencePattern, Long scheduleId);

    /**
     *
     * @param workgroupId
     * @param year
     * @return
     */
    @Query( " SELECT DISTINCT c" +
            " FROM Course c, Schedule sch" +
            " WHERE c.schedule = sch" +
            " AND sch.year = :year " +
            " AND sch.workgroup.id = :workgroupId " +
            " AND c.id NOT IN (SELECT sg.course.id FROM SectionGroup sg, Section s WHERE s.sectionGroup = sg)")
    List<Course> findChildlessByWorkgroupIdAndYear(
    @Param("workgroupId") long workgroupId,
    @Param("year") long year);

    @Query( " SELECT DISTINCT c" +
            " FROM Course c, SectionGroup sg, Section s, Schedule sch" +
            " WHERE sg.course = c" +
            " AND s.sectionGroup = sg" +
            " AND c.schedule = sch" +
            " AND sch.year = :year" +
            " AND sch.workgroup.id = :workgroupId" +
            " AND (s.visible = true OR s.visible IS NULL)")
    List<Course> findVisibleByWorkgroupIdAndYear(
            @Param("workgroupId") long workgroupId,
            @Param("year") long year);

    List<Course> findBySubjectCodeAndCourseNumberAndScheduleId(String subjectCode, String courseNumber, Long scheduleId);
}
