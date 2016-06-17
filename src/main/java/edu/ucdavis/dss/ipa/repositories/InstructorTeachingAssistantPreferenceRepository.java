package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

public interface InstructorTeachingAssistantPreferenceRepository extends CrudRepository<InstructorTeachingAssistantPreference, Long> {

	InstructorTeachingAssistantPreference findById(Long id);

}