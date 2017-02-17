package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TeachingCallResponseRepository extends CrudRepository<TeachingCallResponse, Long> {

	@Query( " SELECT tr" +
			" FROM TeachingAssignment ta, TeachingCallResponse tr" +
			" WHERE ta.sectionGroup.id = :sectionGroupId" +
			" AND ta.instructor = tr.instructor" +
			" AND tr.termCode = :termCode")
    List<TeachingCallResponse> findBySectionGroupIdAndTermCode(
    		@Param("sectionGroupId") long sectionGroupId,
			@Param("termCode") String termCode);

	List<TeachingCallResponse> findByScheduleIdAndTermCode(long scheduleId, String termCode);

	TeachingCallResponse findOneByScheduleIdAndInstructorIdAndTermCode(long scheduleId, long instructorId, String termCode);

	List<TeachingCallResponse> findByScheduleId(long scheduleId);
}