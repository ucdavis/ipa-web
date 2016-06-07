package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;

@Validated
public interface CourseOfferingService {

	CourseOffering saveCourseOffering(CourseOffering courseOffering);

	CourseOffering findOrCreateOneByCourseOfferingGroupAndTermCode(CourseOfferingGroup cog, String termCode);

	CourseOffering findCourseOfferingById(long l);

	void deleteByCourseOfferingGroupAndTermCode(CourseOfferingGroup cog, String termCode);

	CourseOffering createCourseOfferingByCourseIdAndTermCodeAndScheduleId(Long courseId, String termCode, Long scheduleId);
}
