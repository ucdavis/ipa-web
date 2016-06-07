package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Activity;

public interface ActivityRepository extends CrudRepository<Activity, Long> {

	void deleteAllBySectionId(long sectionId);

}
