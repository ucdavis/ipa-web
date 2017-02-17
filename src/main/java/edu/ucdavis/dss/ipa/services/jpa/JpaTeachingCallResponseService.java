package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.TeachingCallResponseRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingCallResponseService;

@Service
public class JpaTeachingCallResponseService implements TeachingCallResponseService {

	@Inject TeachingCallResponseRepository teachingCallResponseRepository;
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
	public List<TeachingCallResponse> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		if (schedule == null) {
			return null;
		}

		return this.teachingCallResponseRepository.findByScheduleIdAndTermCode(schedule.getId(), termCode);
	}

	@Override
	public TeachingCallResponse findOrCreateOneByScheduleIdAndInstructorIdAndTermCode(
			Long scheduleId, long instructorId, String termCode) {
		Schedule schedule = scheduleService.findById(scheduleId);
		Instructor instructor = instructorService.getOneById(instructorId);

		if (instructor == null || schedule == null) {
			return null;
		}

		TeachingCallResponse teachingCallResponse = this.teachingCallResponseRepository.findOneByScheduleIdAndInstructorIdAndTermCode(scheduleId, instructorId, termCode);

		if (teachingCallResponse == null) {
			teachingCallResponse = new TeachingCallResponse();
			teachingCallResponse.setInstructor(instructor);
			teachingCallResponse.setSchedule(schedule);
			teachingCallResponse.setTermCode(termCode);
			teachingCallResponse.setAvailabilityBlob(TeachingCallResponse.DefaultAvailabilityBlob());
			this.save(teachingCallResponse);
		}

		return teachingCallResponse;
	}
}
