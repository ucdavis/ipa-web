package edu.ucdavis.dss.ipa.api.components.term.views;

import edu.ucdavis.dss.ipa.entities.Building;

public class TermBuildingView {
	private long id;
	private String name;

	public TermBuildingView(Building building) {
		if (building == null) return;
		setId(building.getId());
		setName(building.getName());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
