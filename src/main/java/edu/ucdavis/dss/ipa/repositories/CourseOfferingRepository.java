package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.Course;
import org.springframework.data.repository.CrudRepository;

public interface CourseOfferingRepository extends CrudRepository<CourseOffering, Long>  {

	CourseOffering findOneByCourseOfferingGroupAndTermCode(Course cog, String termCode);

}
