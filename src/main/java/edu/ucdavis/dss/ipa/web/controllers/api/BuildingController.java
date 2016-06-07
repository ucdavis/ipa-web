package edu.ucdavis.dss.ipa.web.controllers.api;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import edu.ucdavis.dss.ipa.entities.Building;
import edu.ucdavis.dss.ipa.services.BuildingService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BuildingController {
	@Inject BuildingService buildingService;

	@RequestMapping(value = "/api/buildings", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Building> getAllBuildings()
	{
		return buildingService.getAllBuildings();
	}

}