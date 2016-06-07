package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;

public interface TeachingCallResponseRepository extends CrudRepository<TeachingCallResponse, Long> {

	List<TeachingCallResponse> findByTeachingCallScheduleIdAndTermCode(Long scheduleId, String termCode);

	List<TeachingCallResponse> findByInstructorIdAndTeachingCallScheduleWorkgroupIdAndTeachingCallScheduleYearIn(long instructorId, long workgroupId, List<Long> years);

	TeachingCallResponse findOneByTeachingCallIdAndInstructorIdAndTermCode(Long teachingCallId, long instructorId, String termCode);
}
