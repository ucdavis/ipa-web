package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface TeachingPreferenceRepository extends CrudRepository<TeachingPreference, Long> {

	TeachingPreference findOneByCourseOfferingIdAndInstructorId(Long courseOfferingId, Long instructorId);

	List<TeachingPreference> findByScheduleIdAndTermCode(Long scheduleId, String termCode);

	List<TeachingPreference> findByScheduleId(long scheduleId);

	List<TeachingPreference> findByCourseOfferingIdIn(List<Long> sectionGroupIds);

}
