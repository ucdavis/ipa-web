package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.repositories.TeachingCallResponseRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallResponseService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;

@Service
public class JpaTeachingCallResponseService implements TeachingCallResponseService {
	@Inject TeachingCallResponseRepository teachingCallResponseRepository;
	@Inject TeachingCallService teachingCallService;
	@Inject ScheduleService scheduleService;
	@Inject InstructorService instructorService;

	@Override
	@Transactional
	public TeachingCallResponse saveTeachingCallResponse(TeachingCallResponse teachingCallResponse)
	{
		return this.teachingCallResponseRepository.save(teachingCallResponse);
	}

	@Override
	public TeachingCallResponse findOneById(Long id) {
		return this.teachingCallResponseRepository.findOne(id);
	}

	@Override
	public void deleteTeachingCallResponseById(Long id) {
		this.teachingCallResponseRepository.delete(id);
	}

	@Override
	public List<TeachingCallResponse> findByTeachingCallAndInstructorLoginId(TeachingCall teachingCall, String loginId) {
		List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
		Instructor instructor = instructorService.getInstructorByLoginId(loginId);
		if (instructor == null) return null;

		for(TeachingCallResponse response : teachingCall.getTeachingCallResponses() ) {
			if( response.getInstructor().getLoginId().equals(instructor.getLoginId()) ) {
				teachingCallResponses.add(response);
			}
		}

		return teachingCallResponses;
	}

	@Override
	public List<TeachingCallResponse> getAvailabilitiesForScheduleTerm(Long scheduleId, String termCode) {
		return this.teachingCallResponseRepository.findByTeachingCallScheduleIdAndTermCode(scheduleId, termCode);
	}

	@Override
	public TeachingCallResponse findOrCreateOneByTeachingCallIdAndInstructorIdAndTerm(
			Long teachingCallId, long instructorId, String termCode) {
		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		Instructor instructor = instructorService.getInstructorById(instructorId);
		if (instructor == null) return null;

		TeachingCallResponse teachingCallResponse = this.teachingCallResponseRepository.findOneByTeachingCallIdAndInstructorIdAndTermCode(teachingCallId, instructorId, termCode);

		if (teachingCallResponse == null) {
			teachingCallResponse = new TeachingCallResponse();
			teachingCallResponse.setInstructor(instructor);
			teachingCallResponse.setTeachingCall(teachingCall);
			teachingCallResponse.setTermCode(termCode);
			teachingCallResponse.setAvailabilityBlob(TeachingCall.DefaultBlob());
			this.saveTeachingCallResponse(teachingCallResponse);
		}

		return teachingCallResponse;
	}
}
