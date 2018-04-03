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
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaCourseService implements CourseService {

	@Inject CourseRepository courseRepository;
	@Inject ScheduleService scheduleService;
	@Inject TagService tagService;
	@Inject SectionService sectionService;

	@Override
	public Course getOneById(Long id) {
		return this.courseRepository.findOne(id);
	}

	private Course save(Course course) {
		return this.courseRepository.save(course);
	}

	/**
	 * Will ensure new course is valid,
	 * and modify section sequenceNumbers if necessary.
	 * @param newCourse
	 * @return
     */
	@Transactional
	public Course update (Course newCourse) {
		Course originalCourse = this.getOneById(newCourse.getId());

		// If changing the course sequencePattern
		if (originalCourse.getSequencePattern().equals(newCourse.getSequencePattern()) == false) {

			// Ensure the sequencePattern is unique within relevant courses
			List<Course> duplicateCourses = this.courseRepository.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
					newCourse.getSubjectCode(), newCourse.getCourseNumber(), newCourse.getSequencePattern(), newCourse.getSchedule().getId());

			if (duplicateCourses.size() > 1) {
				return null;
			}

			// Rebuild section sequenceNumbers
			for (SectionGroup sectionGroup : originalCourse.getSectionGroups()) {
				for (Section section : sectionGroup.getSections()) {
					this.sectionService.updateSequenceNumber(section.getId(), newCourse.getSequencePattern());
				}
			}
		}

		originalCourse.setTitle(newCourse.getTitle());
		originalCourse.setUnitsLow(newCourse.getUnitsLow());
		originalCourse.setUnitsHigh(newCourse.getUnitsHigh());
		originalCourse.setSequencePattern(newCourse.getSequencePattern());

		return this.courseRepository.save(originalCourse);
	}

	/**
	 * Sets unitsLow and unitsHigh to given value and saves course to database
	 * @param course - a course object from the database
	 * @param unitsLow - units low value
	 * @param unitsHigh - units high value
	 * @return - the updated course
     */
	@Transactional
	public Course updateUnits(Course course, Float unitsLow, Float unitsHigh) {
		course.setUnitsLow(unitsLow);
		course.setUnitsHigh(unitsHigh);

		return this.courseRepository.save(course);
	}

	@Override
	public Course create(Course course) {
		return this.save(course);
	}

	@Override
	public Course findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(String subjectCode, String courseNumber, String sequencePattern, long scheduleId) {
		return courseRepository.findOneBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(subjectCode, courseNumber, sequencePattern, scheduleId);
	}

	@Override
	public List<Course> findByUnitsLow(Float unitsLow) {
		return courseRepository.findByUnitsLow(unitsLow);
	}

	@Override
	public List<Course> getAllCourses() {
		return (List<Course>) this.courseRepository.findAll();
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

	/**
	 * Attempts to match an existing course based on subjectCode, courseNumber, sequencePattern, effectiveTermCode, schedule.
	 * If none is found, will generate a new course and identify a similar course from past years as a model for what tags should be generated.
	 *
	 * @param courseDTO
	 * @return
	 */
	@Override
	public Course findOrCreateByCourse(Course courseDTO) {
		Course course = courseRepository.findOneBySubjectCodeAndCourseNumberAndSequencePatternAndEffectiveTermCodeAndSchedule(
				courseDTO.getSubjectCode(), courseDTO.getCourseNumber(), courseDTO.getSequencePattern(), courseDTO.getEffectiveTermCode(), courseDTO.getSchedule());

		if (course != null) {
			return course;
		}

		String title = courseDTO.getTitle();

		// Get the meta data (title, tags) from previous offerings if any
		List<Tag> tags = new ArrayList<>();

		Course matchingCourse = courseRepository.findBySubjectCodeAndCourseNumberAndSequencePatternAndEffectiveTermCodeAndHasTags(
				courseDTO.getSubjectCode(), courseDTO.getCourseNumber(), courseDTO.getSequencePattern(), courseDTO.getEffectiveTermCode(), courseDTO.getSchedule().getWorkgroup().getId());
		if (matchingCourse != null) {
			title = matchingCourse.getTitle();
			tags.addAll(matchingCourse.getTags());
		}

		course = new Course();
		course.setSubjectCode(courseDTO.getSubjectCode());
		course.setCourseNumber(courseDTO.getCourseNumber());
		course.setSequencePattern(courseDTO.getSequencePattern());
		course.setTitle(title);
		course.setEffectiveTermCode(courseDTO.getEffectiveTermCode());
		course.setSchedule(courseDTO.getSchedule());
		course.setTags(tags);
		courseRepository.save(course);

		return course;
	}

	@Transactional
	@Override
	public void deleteMultiple(List<Long> courseIds) {
		for (Long courseId : courseIds) {
			this.delete(courseId);
		}
	}

	@Override
	public void massAddTagsToCourses(List<Long> tagsToAdd, List<Long> tagsToRemove, List<Long> courseIds) {
		for (Long courseId : courseIds) {
			Course course = this.getOneById(courseId);

			for (Long tagId: tagsToAdd) {
				Tag tag = tagService.getOneById(tagId);
				this.addTag(course, tag);
			}

			for (Long tagId: tagsToRemove) {
				Tag tag = tagService.getOneById(tagId);
				this.removeTag(course, tag);
			}
		}
	}
}
