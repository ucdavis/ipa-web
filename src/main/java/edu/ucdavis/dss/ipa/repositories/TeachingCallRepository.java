package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingCall;

import java.util.List;

public interface TeachingCallRepository extends CrudRepository<TeachingCall, Long> {

	TeachingCall findFirstByScheduleId(long scheduleId);

	List<TeachingCall> findByScheduleId(long scheduleId);
}
