package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingCall;

public interface TeachingCallRepository extends CrudRepository<TeachingCall, Long> {

	TeachingCall findFirstByScheduleId(long scheduleId);

}
