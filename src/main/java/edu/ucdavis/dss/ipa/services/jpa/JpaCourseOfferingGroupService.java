package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Track;
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
	public CourseOfferingGroup getCourseOfferingGroupById(Long id) {
		return this.courseOfferingGroupRepository.findOne(id);
	}

	@Override
	@Transactional
	public CourseOfferingGroup findOrCreateCourseOfferingGroupByCourseAndScheduleId( Long scheduleId,
			@Validated Course course) {
		CourseOfferingGroup cog = this.courseOfferingGroupRepository.findByCourseIdAndScheduleId(course.getId(), scheduleId);

		if(cog != null) {
			return cog;
		} else {
			return this.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);
		}
	}

	@Override
	@Transactional
	public CourseOfferingGroup createCourseOfferingGroupByCourseAndScheduleId( Long scheduleId, @Validated Course course) {

		CourseOfferingGroup cog =
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

			cog = new CourseOfferingGroup();
			cog.setSchedule(schedule);
			cog.setCourse(savedCourse);
			cog.setTitle(course.getTitle());
			cog = this.courseOfferingGroupRepository.save(cog);
		}

		String trackName = Character.getNumericValue(course.getCourseNumber().charAt(0)) < 2 ? "Undergraduate" : "Graduate";
		Track track = trackService.findOrCreateTrackByWorkgroupAndTrackName(schedule.getWorkgroup(), trackName);
		this.addTrackToCourseOfferingGroup(cog.getId(), track);

		return this.courseOfferingGroupRepository.save(cog);
	}
	
	@Override
	public CourseOfferingGroup saveCourseOfferingGroup(CourseOfferingGroup courseOfferingGroup) {
		return this.courseOfferingGroupRepository.save(courseOfferingGroup);
	}

	@Override
	public boolean deleteCourseOfferingGroupById(Long id) {
		CourseOfferingGroup courseOfferingGroup = this.getCourseOfferingGroupById(id);
		
		if (courseOfferingGroup == null) {
			return false;
		}

		try {
			courseOfferingGroup.setTracks(new ArrayList<Track>());
			this.saveCourseOfferingGroup(courseOfferingGroup);
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
		CourseOfferingGroup courseOfferingGroup = this.courseOfferingGroupRepository.findOne(courseOfferingGroupId);
		SectionGroup coWithNoTermCode = null;

		if (courseOfferingGroup != null) {
			for (CourseOffering courseOffering : courseOfferingGroup.getCourseOfferings()) {
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
			// No CourseOfferingGroup found for this id
			return null;
		}
	}

	@Override
	public List<Track> getTracksByCourseOfferingGroupId(Long id) {
		CourseOfferingGroup courseOfferingGroup = this.getCourseOfferingGroupById(id);
		return courseOfferingGroup.getTracks();
	}

	@Override
	public CourseOfferingGroup addTrackToCourseOfferingGroup(Long id, Track track) {
		CourseOfferingGroup courseOfferingGroup = this.getCourseOfferingGroupById(id);
		List<Track> tracks = courseOfferingGroup.getTracks();
		if(!tracks.contains(track)) {
			tracks.add(track);
		}
		courseOfferingGroup.setTracks(tracks);
		return this.courseOfferingGroupRepository.save(courseOfferingGroup);
	}

//	@Override
//	@Transactional
//	public List<CourseOfferingGroup> getOtherScheduleOfferingsForCourse(
//			Long courseOfferingGroupId) {
//
//		CourseOfferingGroup courseOfferingGroup = this.getCourseOfferingGroupById(courseOfferingGroupId);
//		List<CourseOfferingGroup> courseOfferingGroups = new ArrayList<CourseOfferingGroup>();
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
//			if (!courseOfferingGroups.contains(sectionGroup.getCourseOfferingGroup())
//			&&	sectionGroup.getCourseOfferingGroup().getId() != courseOfferingGroupId) {
//				courseOfferingGroups.add(sectionGroup.getCourseOfferingGroup());
//			}
//		}
//		return courseOfferingGroups;
//	}

	@Override
	public CourseOfferingGroup setCourseSubject(Long id, String subject) {
		CourseOfferingGroup courseOfferingGroup = this.getCourseOfferingGroupById(id);

		if (courseOfferingGroup == null || this.scheduleService.isScheduleClosed(courseOfferingGroup.getSchedule().getId()))
			return courseOfferingGroup;

		// Clearing the title and hitting save should revert to the original Course title
		if (subject == null || subject.isEmpty()) {
			subject = courseOfferingGroup.getCourse().getTitle();
		}

		courseOfferingGroup.setTitle(subject);
		return this.saveCourseOfferingGroup(courseOfferingGroup);
	}

	@Override
	@Transactional
	public CourseOfferingGroup findOrCreateCourseOfferingGroupByScheduleIdAndCourseId( Long scheduleId,
			Long courseId) {
		Course course = this.courseService.findOneById(courseId);
		CourseOfferingGroup cog = this.courseOfferingGroupRepository.findByCourseIdAndScheduleId(courseId, scheduleId);

		if(cog != null) {
			return cog;
		} else {
			return this.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);
		}
	}
}
