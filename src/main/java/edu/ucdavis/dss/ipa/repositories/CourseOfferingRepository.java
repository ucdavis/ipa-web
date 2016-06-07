package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;

public interface CourseOfferingRepository extends CrudRepository<CourseOffering, Long>  {

	CourseOffering findOneByCourseOfferingGroupAndTermCode(CourseOfferingGroup cog, String termCode);

}
