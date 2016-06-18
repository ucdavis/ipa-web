package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.CourseRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TagService;

@Service
public class JpaCourseService implements CourseService {

	@Inject CourseRepository courseRepository;
	@Inject SectionGroupRepository sectionGroupRepository;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject InstructorService instructorService;
	@Inject
	TagService tagService;
	@Inject CourseService courseService;
	
	@Override
	public Course getOneById(Long id) {
		return this.courseRepository.findOne(id);
	}

	@Override
	@Transactional
	public Course findOrCreateByDwCourseAndScheduleIdAndSequencePattern(DwCourse dwCourse, Long scheduleId, String sequencePattern) {
		Course course = this.courseRepository.
				findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
						dwCourse.getSubject().getCode(), dwCourse.getCourseNumber(), sequencePattern, scheduleId);

		if(course != null) {
			return course;
		} else {
			return this.createByDwCourseAndScheduleIdAndSequencePattern(dwCourse, scheduleId, sequencePattern);
		}
	}

	@Transactional
	private Course createByDwCourseAndScheduleIdAndSequencePattern(DwCourse dwCourse, Long scheduleId, String sequencePattern) {

		Schedule schedule = this.scheduleService.findById(scheduleId);
		if (schedule == null) {
			Exception e = new Exception("Could not find schedule with Id: " + scheduleId);
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}

		Course course = new Course();
		course.setSchedule(schedule);
		course.setTitle(dwCourse.getTitle());
		course.setCourseNumber(dwCourse.getCourseNumber());
		course.setEffectiveTermCode(dwCourse.getEffectiveTermCode());
		course.setSequencePattern(sequencePattern);
		course.setSubjectCode(dwCourse.getSubjectCode());
		course.setUnitsHigh(dwCourse.getUnitsMax());
		course.setUnitsLow(dwCourse.getUnitsMin());
		course = this.courseRepository.save(course);

		String trackName = Character.getNumericValue(dwCourse.getCourseNumber().charAt(0)) < 2 ? "Undergraduate" : "Graduate";
		Tag tag = tagService.findOrCreateTrackByWorkgroupAndTrackName(schedule.getWorkgroup(), trackName);
		this.addTag(course.getId(), tag);

		return this.courseRepository.save(course);
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
	public Course addTag(Long id, Tag tag) {
		Course course = this.getOneById(id);
		List<Tag> tags = course.getTags();
		if(!tags.contains(tag)) {
			tags.add(tag);
		}
		course.setTags(tags);
		return this.courseRepository.save(course);
	}

	@Override
	public Course setCourseSubject(Long id, String subject) {
		Course course = this.getOneById(id);

		if (course == null || this.scheduleService.isScheduleClosed(course.getSchedule().getId()))
			return course;

		return this.save(course);
	}

}
