package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.CourseOfferingRepository;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.CourseOfferingService;
import edu.ucdavis.dss.ipa.services.CourseService;

@Service
public class JpaCourseOfferingService implements CourseOfferingService {

	@Inject CourseOfferingRepository courseOfferingRepository;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	@Inject CourseService courseService;

	@Override
	public CourseOffering saveCourseOffering(CourseOffering courseOffering) {
		return this.courseOfferingRepository.save(courseOffering);
	}

	@Override
	public CourseOffering findOrCreateOneByCourseOfferingGroupAndTermCode(Course cog, String termCode) {
		CourseOffering courseOffering = this.courseOfferingRepository.findOneByCourseOfferingGroupAndTermCode(cog, termCode);

		if (courseOffering == null) {
			courseOffering = new CourseOffering();
			courseOffering.setCourse(cog);
			courseOffering.setTermCode(termCode);
			courseOffering.setSeatsTotal(0L);

			this.saveCourseOffering(courseOffering);
		}

		return courseOffering;
	}

	@Override
	public CourseOffering findCourseOfferingById(long courseOfferingId) {
		return this.courseOfferingRepository.findOne(courseOfferingId);
	}

	@Override
	public void deleteByCourseOfferingGroupAndTermCode(Course cog, String termCode) {
		CourseOffering courseOffering = this.courseOfferingRepository.findOneByCourseOfferingGroupAndTermCode(cog, termCode);
		if (courseOffering != null) {
			this.courseOfferingRepository.delete(courseOffering.getId());
		}
	}

	@Override
	public CourseOffering createCourseOfferingByCourseIdAndTermCodeAndScheduleId(Long courseId, String termCode, Long scheduleId) {
		Course course = courseService.findOneById(courseId);

		Course courseOfferingGroup = courseOfferingGroupService.findOrCreateCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);

		return this.findOrCreateOneByCourseOfferingGroupAndTermCode(courseOfferingGroup, termCode);
	}
}
