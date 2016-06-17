package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseOfferingGroupRepository extends CrudRepository<Course, Long> {

	Course findByCourseIdAndScheduleId(long id, Long scheduleId);
}
