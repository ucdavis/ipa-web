package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Course;

@Validated
public interface CourseOfferingService {

	CourseOffering saveCourseOffering(CourseOffering courseOffering);

	CourseOffering findOrCreateOneByCourseOfferingGroupAndTermCode(Course cog, String termCode);

	CourseOffering findCourseOfferingById(long l);

	void deleteByCourseOfferingGroupAndTermCode(Course cog, String termCode);

	CourseOffering createCourseOfferingByCourseIdAndTermCodeAndScheduleId(Long courseId, String termCode, Long scheduleId);
}
