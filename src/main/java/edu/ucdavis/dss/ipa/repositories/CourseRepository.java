package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Course;

public interface CourseRepository extends CrudRepository<Course, Long> {
	Course findOneByEffectiveTermCodeAndSubjectCodeAndCourseNumber(String effectiveTermCode, String subjectCode, String courseNumber);

}
