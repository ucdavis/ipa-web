package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.TeachingCall;

import java.util.List;

@Validated
public interface TeachingCallService {

	public TeachingCall findOneById(Long id);

	public TeachingCall create(long scheduleId, TeachingCall teachingCallDTO);

	public TeachingCall findFirstByScheduleId(long id);

	public List<TeachingCall> findByScheduleId(long id);

	public TeachingCall findOneByUserIdAndScheduleId(long userId, long id);

	void deleteById(long teachingCallId);
}
