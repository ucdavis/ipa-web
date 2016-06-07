package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.InstructorTeachingAssistantPreference;
import edu.ucdavis.dss.ipa.entities.TeachingAssistantPreference;

public interface InstructorTeachingAssistantPreferenceRepository extends CrudRepository<InstructorTeachingAssistantPreference, Long> {

	InstructorTeachingAssistantPreference findById(Long id);

}