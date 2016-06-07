package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;

public interface CourseOfferingGroupRepository extends CrudRepository<CourseOfferingGroup, Long> {

	CourseOfferingGroup findByCourseIdAndScheduleId(long id, Long scheduleId);
}
