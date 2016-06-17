package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public interface CourseService {

	Course saveCourse(@NotNull @Valid Course course);

	Course findOneById(Long id);
	
	Course getOneByEffectiveTermAndSubjectCodeAndCourseNumber(String effectiveTermCode, String subjectCode, String courseNumber);
	
	List<Course> getAllCourses();

	Course findOrCreateByEffectiveTermAndSubjectCodeAndCourseNumberAndTitle(String effectiveTermCode, String subjectCode, String courseNumber, String title);
}
