package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

@Validated
public interface CourseService {

	Course getOneById(Long id);

	Course findOrCreateByDwCourseAndScheduleIdAndSequencePattern(DwCourse dwCourse, Long scheduleId, String sequencePattern);

	Course save(Course course);

	boolean delete(Long id);

	Course addTag(Long courseId, Tag tag);

	Course setCourseSubject(Long id, String subject);

}
