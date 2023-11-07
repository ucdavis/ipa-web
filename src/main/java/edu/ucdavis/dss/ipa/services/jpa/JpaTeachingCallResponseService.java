package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Arrays;
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
		return this.teachingCallResponseRepository.findById(id).orElse(null);
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

	@Override
	public List<TeachingCallResponse> findOrCreateByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
		List<TeachingCallResponse> teachingCallResponses = new ArrayList<>();

		Schedule schedule = scheduleService.findById(scheduleId);
		String year = String.valueOf(schedule.getYear());
		String endYear = String.valueOf(schedule.getYear()+1);

		String[] termList = {"01", "02", "03", "05", "06", "07", "08", "09", "10"};
		List<String> terms = new ArrayList<>(Arrays.asList(termList));
		List<String> termCodes = new ArrayList<>();

		for (String term : terms) {
			String termCode = "";

			if (Integer.valueOf(term) > 4) {
				termCode = year + term;
			} else {
				termCode = endYear + term;
			}

			termCodes.add(termCode);
		}



		for (String termCode : termCodes) {
			TeachingCallResponse teachingCallResponse = findOrCreateOneByScheduleIdAndInstructorIdAndTermCode(scheduleId, instructorId, termCode);
			teachingCallResponses.add(teachingCallResponse);
		}

		return teachingCallResponses;
	}
}
