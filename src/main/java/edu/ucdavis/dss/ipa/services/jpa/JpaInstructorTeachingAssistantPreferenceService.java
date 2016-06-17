package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.InstructorTeachingAssistantPreferenceRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTeachingAssistantPreferenceService;
import edu.ucdavis.dss.ipa.services.ScheduleService;

@Service
public class JpaInstructorTeachingAssistantPreferenceService implements InstructorTeachingAssistantPreferenceService {
	@Inject InstructorTeachingAssistantPreferenceRepository instructorTeachingAssistantPreferenceRepository;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;
	
	@Override
	public InstructorTeachingAssistantPreference saveInstructorTeachingAssistantPreference(
			InstructorTeachingAssistantPreference instructorTeachingAssistantPreference) {
		return instructorTeachingAssistantPreferenceRepository.save(instructorTeachingAssistantPreference);
	}

	@Override
	public void deleteInstructorTeachingAssistantPreferenceServiceById(Long id) {
		instructorTeachingAssistantPreferenceRepository.delete(id);
	}

	@Override
	public InstructorTeachingAssistantPreference findOneById(Long id) {
		return instructorTeachingAssistantPreferenceRepository.findById(id);
	}
	
	@Override
	public List<InstructorTeachingAssistantPreference> getInstructorTeachingAssistantPreferencesByScheduleIdAndTermCodeAndInstructorId(Long scheduleId, String termCode, Long instructorId) {
		List<InstructorTeachingAssistantPreference> instructorTeachingAssistantPreferences = new ArrayList<InstructorTeachingAssistantPreference>();
		Instructor instructor = instructorService.getInstructorById(instructorId);
		Schedule schedule = scheduleService.findById(scheduleId);

		// Find instructorTaPrefs from the same schedule/term/ta
		for(InstructorTeachingAssistantPreference instructorTeachingAssistantPreference: instructor.getInstructorTeachingAssistantPreferences()) {
			if( instructorTeachingAssistantPreference.getSectionGroup().getCourseOfferingGroup().getSchedule().equals(schedule) && instructorTeachingAssistantPreference.getSectionGroup().getTermCode().equals(termCode) ) {
				instructorTeachingAssistantPreferences.add(instructorTeachingAssistantPreference);
			}
		}
		return instructorTeachingAssistantPreferences;
	}

	@Override
	public void sortInstructorTeachingAssistantPreferences(Long instructorId, List<Long> sortedInstructorTeachingPreferenceIds) {
		Long rank = 1L;
		
		for(Long id : sortedInstructorTeachingPreferenceIds) {
			InstructorTeachingAssistantPreference instructorTeachingAssistantPreference = this.findOneById(id);

			if( instructorTeachingAssistantPreference.getInstructor().getId() == instructorId ) {
				instructorTeachingAssistantPreference.setRank(rank);
				this.saveInstructorTeachingAssistantPreference(instructorTeachingAssistantPreference);
				rank++;
			}
		}
	}

}
