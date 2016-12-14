package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface CourseService {

	Course getOneById(Long id);

	Course save(Course course);

	boolean delete(Long id);

	Course addTag(Course course, Tag tag);

	Course removeTag(Course course, Tag tag);

	List<Course> findByTagId(Long id);

	List<Course> findByWorkgroupIdAndYear(long workgroupId, long year);

	List<Course> findVisibleByWorkgroupIdAndYear(long id, long year);

	List<Course> findBySubjectCodeAndCourseNumberAndScheduleId(String subjectCode, String courseNumber, long id);

	Course findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
			String subjectCode, String courseNumber, String sequencePattern, String title, String effectiveTermCode, Schedule schedule, boolean copyMetaData);
}
