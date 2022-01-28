package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.dw.dto.DwSearchResultSection;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface CourseService {

	Course getOneById(Long id);

	Course update(Course course);

	boolean delete(Long id);

	Course addTag(Course course, Tag tag);

	Course removeTag(Course course, Tag tag);

	List<Course> findByWorkgroupIdAndYear(long workgroupId, long year);

	List<Course> findVisibleByWorkgroupIdAndYear(long id, long year);

	List<Course> findBySubjectCodeAndCourseNumberAndScheduleId(String subjectCode, String courseNumber, long id);

	List<Course> findByUnitsLow(Float unitsLow);

	List<Course> findByScheduleIn(List<Schedule> scheduleIds);

	Course findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
			String subjectCode, String courseNumber, String sequencePattern, String title, String effectiveTermCode, Schedule schedule, boolean copyMetaData);

	Course create(Course course);

	Course findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(String subjectCode, String courseNumber, String sequencePattern, long id);

	List<Course> getAllCourses();

	Course updateUnits(Course course, Float unitsLow, Float unitsHigh);

	Course updateFromDwSearchResultSection(Course course, DwSearchResultSection dwSearchResultSection);

	Course findOrCreateByCourse(Course courseDTO);

	void deleteMultiple(List<Long> courseId);

	void massAddTagsToCourses(List<Long> tagsToAdd, List<Long> tagsToRemove, List<Long> courseIds);

	List<Course> findByScheduleId(long l);
}
