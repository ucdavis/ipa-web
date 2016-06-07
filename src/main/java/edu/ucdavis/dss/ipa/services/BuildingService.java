package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Building;

@Validated
public interface BuildingService {

	Building saveBuilding(@NotNull @Valid Building building);

	Building findOneById(Long id);

	Building findByName(String buildingName);

	List<Building> getAllBuildings();

	Building findOrCreateByName(String buildingName);
}
