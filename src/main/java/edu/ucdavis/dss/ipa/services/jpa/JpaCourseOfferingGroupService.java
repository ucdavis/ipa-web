package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.CourseOfferingGroupRepository;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TrackService;

@Service
public class JpaCourseOfferingGroupService implements CourseOfferingGroupService {

	@Inject CourseOfferingGroupRepository courseOfferingGroupRepository;
	@Inject SectionGroupRepository sectionGroupRepository;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject InstructorService instructorService;
	@Inject TrackService trackService;
	@Inject CourseService courseService;
	
	@Override
	public Course getCourseOfferingGroupById(Long id) {
		return this.courseOfferingGroupRepository.findOne(id);
	}

	@Override
	@Transactional
	public Course findOrCreateCourseOfferingGroupByCourseAndScheduleId(Long scheduleId,
																	   @Validated Course course) {
		Course cog = this.courseOfferingGroupRepository.findByCourseIdAndScheduleId(course.getId(), scheduleId);

		if(cog != null) {
			return cog;
		} else {
			return this.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);
		}
	}

	@Override
	@Transactional
	public Course createCourseOfferingGroupByCourseAndScheduleId(Long scheduleId, @Validated Course course) {

		Course cog =
				this.sectionGroupRepository.findBySubjectCodeAndCourseNumberAndScheduleIdAndTitle(
						course.getSubjectCode(),
						course.getCourseNumber(),
						scheduleId,
						course.getTitle())
				.stream().findFirst().map(possibleSectionGroup -> possibleSectionGroup.getCourseOfferingGroup())
				.orElse(null);

		Schedule schedule = this.scheduleService.findById(scheduleId);
		if (schedule == null) {
			Exception e = new Exception("Could not find schedule with Id: " + scheduleId);
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}

		if (cog == null) {
			Course savedCourse = courseService.findOrCreateByEffectiveTermAndSubjectCodeAndCourseNumberAndTitle(
					course.getEffectiveTermCode(),
					course.getSubjectCode(),
					course.getCourseNumber(),
					course.getTitle());

			cog = new Course();
			cog.setSchedule(schedule);
			cog.setCourse(savedCourse);
			cog.setTitle(course.getTitle());
			cog = this.courseOfferingGroupRepository.save(cog);
		}

		String trackName = Character.getNumericValue(course.getCourseNumber().charAt(0)) < 2 ? "Undergraduate" : "Graduate";
		Tag tag = trackService.findOrCreateTrackByWorkgroupAndTrackName(schedule.getWorkgroup(), trackName);
		this.addTrackToCourseOfferingGroup(cog.getId(), tag);

		return this.courseOfferingGroupRepository.save(cog);
	}
	
	@Override
	public Course saveCourseOfferingGroup(Course course) {
		return this.courseOfferingGroupRepository.save(course);
	}

	@Override
	public boolean deleteCourseOfferingGroupById(Long id) {
		Course course = this.getCourseOfferingGroupById(id);
		
		if (course == null) {
			return false;
		}

		try {
			course.setTags(new ArrayList<Tag>());
			this.saveCourseOfferingGroup(course);
			this.courseOfferingGroupRepository.delete(id);

			return true;
		} catch (EmptyResultDataAccessException e) {

			// Could not delete the course offering group because it doesn't exist.
			// Don't worry about this.
		}

		return false;
	}

	@Override
	public SectionGroup findSectionGroupByOfferingGroupIdAndTermCodeAndSequence(Long courseOfferingGroupId, String termCode, String sequence) {
		Course course = this.courseOfferingGroupRepository.findOne(courseOfferingGroupId);
		SectionGroup coWithNoTermCode = null;

		if (course != null) {
			for (CourseOffering courseOffering : course.getSectionGroups()) {
				for (SectionGroup sectionGroup : courseOffering.getSectionGroups()) {
					if (sectionGroup.getTermCode() == null) {
						// In the case the termCode is null, save it but keep looping to see if there is a matching termcode
						coWithNoTermCode = sectionGroup;
					} else if (	sectionGroup.getTermCode().equals(termCode)
							&&	sectionGroup.getSections() != null
							&&	sectionGroup.getSections().size() > 0
							&&	sectionGroup.getSections().get(0).getSequenceNumber().equals(sequence)) {
						// Return a course offering if the termcode matches
						return sectionGroup;
					}
				}
			}

			// If the above loop is over and we found a course offering with an empty termcode, return it
			return coWithNoTermCode;
		} else {
			// No Course found for this id
			return null;
		}
	}

	@Override
	public List<Tag> getTracksByCourseOfferingGroupId(Long id) {
		Course course = this.getCourseOfferingGroupById(id);
		return course.getTags();
	}

	@Override
	public Course addTrackToCourseOfferingGroup(Long id, Tag tag) {
		Course course = this.getCourseOfferingGroupById(id);
		List<Tag> tags = course.getTags();
		if(!tags.contains(tag)) {
			tags.add(tag);
		}
		course.setTags(tags);
		return this.courseOfferingGroupRepository.save(course);
	}

//	@Override
//	@Transactional
//	public List<Course> getOtherScheduleOfferingsForCourse(
//			Long courseOfferingGroupId) {
//
//		Course courseOfferingGroup = this.getCourseOfferingGroupById(courseOfferingGroupId);
//		List<Course> courseOfferingGroups = new ArrayList<Course>();
//
//		if (courseOfferingGroup.getSectionGroups() == null
//		||	courseOfferingGroup.getSectionGroups().size() == 0) return null;
//
//		SectionGroup co = courseOfferingGroup.getSectionGroups().get(0);
//		String sectionSequence = this.sectionGroupService.getSectionGroupSequence(co);
//
//		List<SectionGroup> sectionGroups = this.sectionGroupRepository.findByCourseIdAndSequencePattern(
//				co.getCourse().getId(),
//				sectionSequence
//			);
//
//		for (SectionGroup sectionGroup : sectionGroups) {
//			if (!courseOfferingGroups.contains(sectionGroup.getCourse())
//			&&	sectionGroup.getCourse().getId() != courseOfferingGroupId) {
//				courseOfferingGroups.add(sectionGroup.getCourse());
//			}
//		}
//		return courseOfferingGroups;
//	}

	@Override
	public Course setCourseSubject(Long id, String subject) {
		Course course = this.getCourseOfferingGroupById(id);

		if (course == null || this.scheduleService.isScheduleClosed(course.getSchedule().getId()))
			return course;

		// Clearing the title and hitting save should revert to the original Course title
		if (subject == null || subject.isEmpty()) {
			subject = course.getCourse().getTitle();
		}

		course.setTitle(subject);
		return this.saveCourseOfferingGroup(course);
	}

	@Override
	@Transactional
	public Course findOrCreateCourseOfferingGroupByScheduleIdAndCourseId(Long scheduleId,
																		 Long courseId) {
		Course course = this.courseService.findOneById(courseId);
		Course cog = this.courseOfferingGroupRepository.findByCourseIdAndScheduleId(courseId, scheduleId);

		if(cog != null) {
			return cog;
		} else {
			return this.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);
		}
	}
}
