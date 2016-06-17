package edu.ucdavis.dss.ipa.services;

import java.util.List;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.SectionGroup;

@Validated
public interface CourseOfferingGroupService {

	Course getCourseOfferingGroupById(Long id);
	SectionGroup findSectionGroupByOfferingGroupIdAndTermCodeAndSequence(Long courseOfferingGroupId, String termCode, String sequence);

	List<Tag> getTracksByCourseOfferingGroupId(Long id);

	Course createCourseOfferingGroupByCourseAndScheduleId(Long scheduleId, Course course);
	Course findOrCreateCourseOfferingGroupByCourseAndScheduleId(Long scheduleId, Course course);

	Course saveCourseOfferingGroup(Course course);

	boolean deleteCourseOfferingGroupById(Long id);

	Course addTrackToCourseOfferingGroup(Long id, Tag tag);

	Course setCourseSubject(Long id, String subject);

	Course findOrCreateCourseOfferingGroupByScheduleIdAndCourseId(Long scheduleId, Long courseId);
}
