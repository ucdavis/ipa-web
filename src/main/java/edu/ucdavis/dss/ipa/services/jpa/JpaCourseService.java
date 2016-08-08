package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.CourseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
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

		String tagName = Character.getNumericValue(dwCourse.getCourseNumber().charAt(0)) < 2 ? "Undergraduate" : "Graduate";

		String UNDERGRADUATE_COLOR = "#9CAF88";
		String GRADUATE_COLOR = "#5B7F95";
		String tagColor = Character.getNumericValue(dwCourse.getCourseNumber().charAt(0)) < 2 ? UNDERGRADUATE_COLOR : GRADUATE_COLOR;

		Tag tag = tagService.findOrCreateByWorkgroupAndName(schedule.getWorkgroup(), tagName, tagColor);
		this.addTag(course, tag);

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
	public List<Course> findByWorkgroupIdAndYear(long id, long year) {
		Workgroup workgroup = workgroupService.findOneById(id);
		Schedule schedule = this.scheduleService.findByWorkgroupAndYear(workgroup, year);
		List<Course> courses = schedule.getCourses();

		return courses;
	}

}
