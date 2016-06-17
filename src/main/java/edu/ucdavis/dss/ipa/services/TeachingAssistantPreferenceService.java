package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

@Validated
public interface TeachingAssistantPreferenceService {
	TeachingAssistantPreference saveTeachingAssistantPreference(@NotNull @Valid TeachingAssistantPreference teachingAssistantPreference);

	void deleteTeachingAssistantPreferenceServiceById(Long id);
	
	TeachingAssistantPreference findOneById(Long id);
	
	List<TeachingAssistantPreference> getTeachingAssistantPreferencesByScheduleIdAndTermCodeAndGraduateStudentId(long scheduleId, String termCode, Long graduateStudentId);

}
