package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.CourseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;

@Service
public class JpaCourseService implements CourseService {

	@Inject CourseRepository courseRepository;
	@Inject SectionGroupRepository sectionGroupRepository;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject InstructorService instructorService;
	@Inject TagService tagService;
	@Inject WorkgroupService workgroupService;

	@Override
	public Course getOneById(Long id) {
		return this.courseRepository.findOne(id);
	}

	@Override
	public Course save(Course course) {
		return this.courseRepository.save(course);
	}

	@Override
	public boolean delete(Long id) {
		Course course = this.getOneById(id);
		
		if (course == null) {
			return false;
		}

		try {
			course.setTags(new ArrayList<Tag>());
			this.save(course);
			this.courseRepository.delete(id);

			return true;
		} catch (EmptyResultDataAccessException e) {

			// Could not delete the course offering group because it doesn't exist.
			// Don't worry about this.
		}

		return false;
	}

	@Override
	public Course addTag(Course course, Tag tag) {
		if (course == null) { return null; }

		List<Tag> tags = course.getTags();
		if(!tags.contains(tag)) {
			tags.add(tag);
		}
		course.setTags(tags);
		return this.courseRepository.save(course);
	}

	@Override
	public Course removeTag(Course course, Tag tag) {
		if (course == null) { return null; }

		List<Tag> tags = course.getTags();
		if(tags.contains(tag)) {
			tags.remove(tag);
		}
		course.setTags(tags);
		return this.courseRepository.save(course);
	}

	@Override
	public List<Course> findByTagId(Long id) {
		Tag tag = tagService.getOneById(id);
		return tag.getCourses();
	}

	@Override
	public List<Course> findByWorkgroupIdAndYear(long workgroupId, long year) {
		Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		return schedule.getCourses();
	}

	@Override
	public List<Course> findVisibleByWorkgroupIdAndYear(long workgroupId, long year) {
		List<Course> visibleCourses = courseRepository.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		List<Course> childlessCourses = courseRepository.findChildlessByWorkgroupIdAndYear(workgroupId, year);
		visibleCourses.addAll(childlessCourses);
		return visibleCourses;
	}

	@Override
	public List<Course> findBySubjectCodeAndCourseNumberAndScheduleId(String subjectCode, String courseNumber, long scheduleId) {
		return courseRepository.findBySubjectCodeAndCourseNumberAndScheduleId(subjectCode, courseNumber, scheduleId);
	}

	@Override
	public Course findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
			String subjectCode, String courseNumber, String sequencePattern, String title, String effectiveTermCode, Schedule schedule, boolean copyMetaData) {

		Course course = courseRepository.findOneBySubjectCodeAndCourseNumberAndSequencePatternAndEffectiveTermCodeAndSchedule(
				subjectCode, courseNumber, sequencePattern, effectiveTermCode, schedule);

		if (course == null) {
			List<Tag> tags = new ArrayList<>();
			// Get the meta data (title, tags) from previous offerings if any
			if (copyMetaData) {
				Course matchingCourse = courseRepository.findBySubjectCodeAndCourseNumberAndSequencePatternAndEffectiveTermCodeAndHasTags(
						subjectCode, courseNumber, sequencePattern, effectiveTermCode, schedule.getWorkgroup().getId());
				if (matchingCourse != null) {
					title = matchingCourse.getTitle();
					tags.addAll(matchingCourse.getTags());
				}
			}

			course = new Course();
			course.setSubjectCode(subjectCode);
			course.setCourseNumber(courseNumber);
			course.setSequencePattern(sequencePattern);
			course.setTitle(title);
			course.setEffectiveTermCode(effectiveTermCode);
			course.setSchedule(schedule);
			course.setTags(tags);
			courseRepository.save(course);
		}

		return course;
	}

}
