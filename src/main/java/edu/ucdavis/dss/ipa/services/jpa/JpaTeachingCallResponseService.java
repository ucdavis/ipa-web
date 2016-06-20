package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.Workgroup;
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
	public TeachingCallResponse save(TeachingCallResponse teachingCallResponse)
	{
		return this.teachingCallResponseRepository.save(teachingCallResponse);
	}

	@Override
	public TeachingCallResponse getOneById(Long id) {
		return this.teachingCallResponseRepository.findOne(id);
	}

	@Override
	public void delete(Long id) {
		this.teachingCallResponseRepository.delete(id);
	}

	@Override
	public List<TeachingCallResponse> findByTeachingCallAndInstructorLoginId(TeachingCall teachingCall, String loginId) {
		List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
		Instructor instructor = instructorService.getOneByLoginId(loginId);
		if (instructor == null) return null;

		for(TeachingCallResponse response : teachingCall.getTeachingCallResponses() ) {
			if( response.getInstructor().getLoginId().equals(instructor.getLoginId()) ) {
				teachingCallResponses.add(response);
			}
		}

		return teachingCallResponses;
	}

	@Override
	public List<TeachingCallResponse> findByScheduleIdAndTermCode(Long scheduleId, String termCode) {
		return this.teachingCallResponseRepository.findByTeachingCallScheduleIdAndTermCode(scheduleId, termCode);
	}

	@Override
	public TeachingCallResponse findOrCreateOneByTeachingCallIdAndInstructorIdAndTermCode(
			Long teachingCallId, long instructorId, String termCode) {
		TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
		Instructor instructor = instructorService.getOneById(instructorId);
		if (instructor == null) return null;

		TeachingCallResponse teachingCallResponse = this.teachingCallResponseRepository.findOneByTeachingCallIdAndInstructorIdAndTermCode(teachingCallId, instructorId, termCode);

		if (teachingCallResponse == null) {
			teachingCallResponse = new TeachingCallResponse();
			teachingCallResponse.setInstructor(instructor);
			teachingCallResponse.setTeachingCall(teachingCall);
			teachingCallResponse.setTermCode(termCode);
			teachingCallResponse.setAvailabilityBlob(TeachingCall.DefaultBlob());
			this.save(teachingCallResponse);
		}

		return teachingCallResponse;
	}

	@Override
	public List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor, int years) {
		if (workgroup == null || instructor == null)
			return null;

		long thisYear = Calendar.getInstance().get(Calendar.YEAR);
		List<Long> yearsList = LongStream.rangeClosed(thisYear - years + 1, thisYear).boxed().collect(Collectors.toList());
		List<TeachingCallResponse> teachingCallResponses = this.teachingCallResponseRepository.findByInstructorIdAndTeachingCallScheduleWorkgroupIdAndTeachingCallScheduleYearIn(instructor.getId(), workgroup.getId(), yearsList);

		return teachingCallResponses;
	}

	@Override
	public List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor) {
		int NUMBER_OF_YEARS = 10;
		return getWorkgroupTeachingCallResponsesByInstructorId(workgroup, instructor, NUMBER_OF_YEARS);
	}

}
