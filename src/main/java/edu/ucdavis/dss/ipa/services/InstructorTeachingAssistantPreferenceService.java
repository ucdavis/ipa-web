package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.InstructorTeachingAssistantPreference;

@Validated
public interface InstructorTeachingAssistantPreferenceService {
	InstructorTeachingAssistantPreference saveInstructorTeachingAssistantPreference(@NotNull @Valid InstructorTeachingAssistantPreference instructorTeachingAssistantPreference);

	void deleteInstructorTeachingAssistantPreferenceServiceById(Long id);
	
	InstructorTeachingAssistantPreference findOneById(Long id);
	
	List<InstructorTeachingAssistantPreference> getInstructorTeachingAssistantPreferencesByScheduleIdAndTermCodeAndInstructorId(Long scheduleId, String termCode, Long instructorId);

	void sortInstructorTeachingAssistantPreferences(Long instructorId, List<Long> sortedInstructorTeachingPreferenceIds);
}
