package edu.ucdavis.dss.ipa.services.jpa;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Building;
import edu.ucdavis.dss.ipa.repositories.BuildingRepository;
import edu.ucdavis.dss.ipa.services.BuildingService;

@Service
public class JpaBuildingService implements BuildingService {
	@Inject BuildingRepository buildingRepository;
	
	@Override
	public Building saveBuilding(Building building)
	{
		return this.buildingRepository.save(building);
	}

	@Override
	public Building findOneById(Long id) {
		return this.buildingRepository.findOne(id);
	}

	@Override
	public Building findByName(String buildingName) {
		return this.buildingRepository.findOneByName(buildingName);
	}

	@Override
	public List<Building> getAllBuildings() {
		return (List<Building>) this.buildingRepository.findAll();
	}

	@Override
	public Building findOrCreateByName(String buildingName) {
		if(buildingName == null) return null;
		
		Building building = this.findByName(buildingName);

		if(building == null) {
			building = new Building();
			building.setName(buildingName);
			building = this.buildingRepository.save(building);
		}

		return building;
	}
}
