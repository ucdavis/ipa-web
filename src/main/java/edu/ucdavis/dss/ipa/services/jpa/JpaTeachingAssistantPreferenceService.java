package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.TeachingAssistantPreferenceRepository;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TeachingAssistantPreferenceService;

@Service
public class JpaTeachingAssistantPreferenceService implements TeachingAssistantPreferenceService {
	@Inject TeachingAssistantPreferenceRepository teachingAssistantPreferenceRepository;
	@Inject GraduateStudentService graduateStudentService;
	@Inject ScheduleService scheduleService;
	
	@Override
	public TeachingAssistantPreference saveTeachingAssistantPreference(TeachingAssistantPreference teachingAssistantPreference) {
		return teachingAssistantPreferenceRepository.save(teachingAssistantPreference);
	}
	
	@Override
	public void deleteTeachingAssistantPreferenceServiceById(Long id) {
		teachingAssistantPreferenceRepository.delete(id);
	}

	@Override
	public TeachingAssistantPreference findOneById(Long id) {
		return teachingAssistantPreferenceRepository.findById(id);
	}

	@Override
	public List<TeachingAssistantPreference> getTeachingAssistantPreferencesByScheduleIdAndTermCodeAndGraduateStudentId(long scheduleId, String termCode, Long graduateStudentId) {
		List<TeachingAssistantPreference> teachingAssistantPreferences = new ArrayList<TeachingAssistantPreference>();
		GraduateStudent graduateStudent = graduateStudentService.findOneById(graduateStudentId);
		Schedule schedule = scheduleService.findById(scheduleId);

		// Find TaPrefs from the same schedule/term/ta
		for(TeachingAssistantPreference teachingAssistantPreference: graduateStudent.getTeachingAssistantPreferences()) {
			if( teachingAssistantPreference.getSectionGroup().getCourseOfferingGroup().getSchedule().equals(schedule) && teachingAssistantPreference.getSectionGroup().getTermCode().equals(termCode) ) {
				teachingAssistantPreferences.add(teachingAssistantPreference);
			}
		}
		return teachingAssistantPreferences;
	}
}
