package edu.ucdavis.dss.ipa.services;

import java.util.List;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Track;

@Validated
public interface CourseOfferingGroupService {

	CourseOfferingGroup getCourseOfferingGroupById(Long id);
	SectionGroup findSectionGroupByOfferingGroupIdAndTermCodeAndSequence(Long courseOfferingGroupId, String termCode, String sequence);

	List<Track> getTracksByCourseOfferingGroupId(Long id);

	CourseOfferingGroup createCourseOfferingGroupByCourseAndScheduleId(Long scheduleId, Course course);
	CourseOfferingGroup findOrCreateCourseOfferingGroupByCourseAndScheduleId(Long scheduleId, Course course);

	CourseOfferingGroup saveCourseOfferingGroup(CourseOfferingGroup courseOfferingGroup);

	boolean deleteCourseOfferingGroupById(Long id);

	CourseOfferingGroup addTrackToCourseOfferingGroup(Long id, Track track);

	CourseOfferingGroup setCourseSubject(Long id, String subject);

	CourseOfferingGroup findOrCreateCourseOfferingGroupByScheduleIdAndCourseId(Long scheduleId, Long courseId);
}
