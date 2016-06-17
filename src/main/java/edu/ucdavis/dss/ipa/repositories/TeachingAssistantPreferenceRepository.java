package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

public interface TeachingAssistantPreferenceRepository extends CrudRepository<TeachingAssistantPreference, Long> {

	TeachingAssistantPreference findById(Long id);

}