package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface CourseService {

	Course getOneById(Long id);

	Course save(Course course);

	boolean delete(Long id);

	Course addTag(Long courseId, Tag tag);

	List<Course> findByTrackId(Long id);

	Course setCourseSubject(Long id, String subject);

}
