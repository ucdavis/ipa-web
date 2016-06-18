package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseRepository extends CrudRepository<Course, Long> {

	Course findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(String subjectCode, String courseNumber, String sequencePattern, Long scheduleId);

}
