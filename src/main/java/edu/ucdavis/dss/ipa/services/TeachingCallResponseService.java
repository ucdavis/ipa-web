package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;

// The availabilityBlob on a teachingCallResponse is a comma delimited string
// It represents availability within a 15 hour window (7am-10pm) over 5 days
// 1 for available, 0 for not
@Validated
public interface TeachingCallResponseService {

	public TeachingCallResponse saveTeachingCallResponse(@NotNull @Valid TeachingCallResponse teachingCallResponse);

	public TeachingCallResponse findOneById(Long id);

	public void deleteTeachingCallResponseById(Long id);

	public List<TeachingCallResponse> findByTeachingCallAndInstructorLoginId(TeachingCall teachingCall, String loginId);

	List<TeachingCallResponse> getAvailabilitiesForScheduleTerm(Long scheduleId, String termCode);

	public TeachingCallResponse findOrCreateOneByTeachingCallIdAndInstructorIdAndTerm(Long teachingCallId, long instructorId, String termCode);
}
