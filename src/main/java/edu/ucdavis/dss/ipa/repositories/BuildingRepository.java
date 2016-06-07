package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Building;

public interface BuildingRepository extends CrudRepository<Building, Long> {

	Building findOneByName(String buildingName);

}
