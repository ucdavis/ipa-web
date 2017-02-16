package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TeachingCallRepository extends CrudRepository<TeachingCall, Long> {

	TeachingCall findFirstByScheduleId(long scheduleId);

	List<TeachingCall> findByScheduleId(long scheduleId);
}
