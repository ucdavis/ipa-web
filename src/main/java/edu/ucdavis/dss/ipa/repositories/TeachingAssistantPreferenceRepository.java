package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.TeachingAssistantPreference;

public interface TeachingAssistantPreferenceRepository extends CrudRepository<TeachingAssistantPreference, Long> {

	TeachingAssistantPreference findById(Long id);

}