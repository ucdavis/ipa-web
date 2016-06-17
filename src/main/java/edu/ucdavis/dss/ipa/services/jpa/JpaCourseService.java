package edu.ucdavis.dss.ipa.services.jpa;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.CourseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaCourseService implements CourseService {
	@Inject CourseRepository courseRepository;
	@Inject WorkgroupService workgroupService;
	
	@Override
	public Course saveCourse(Course course) {
		return this.courseRepository.save(course);
	}

	@Override
	public Course findOneById(Long id) {
		return this.courseRepository.findOne(id);
	}

	@Override
	public Course findOrCreateByEffectiveTermAndSubjectCodeAndCourseNumberAndTitle(String effectiveTermCode, String subjectCode, String courseNumber, String title) {
		Course course = this.getOneByEffectiveTermAndSubjectCodeAndCourseNumber(effectiveTermCode, subjectCode, courseNumber);

		if (course == null) {
			course = new Course();
			course.setTitle(title);
			course.setEffectiveTermCode(effectiveTermCode);
			course.setSubjectCode(subjectCode);
			course.setCourseNumber(courseNumber);
			
			course = this.saveCourse(course);
		}
		
		return course;
	}
	
	@Override
	public Course getOneByEffectiveTermAndSubjectCodeAndCourseNumber(String effectiveTerm, String subjectCode, String courseNumber) {
		return this.courseRepository.findOneByEffectiveTermCodeAndSubjectCodeAndCourseNumber(effectiveTerm, subjectCode, courseNumber);
	}

	@Override
	public List<Course> getAllCourses() {
		return (List<Course>) this.courseRepository.findAll();
	}
	
}
