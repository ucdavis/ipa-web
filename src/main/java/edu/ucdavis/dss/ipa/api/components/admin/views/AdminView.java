package edu.ucdavis.dss.ipa.api.components.admin.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class AdminView {
	private List<Workgroup> workgroups = new ArrayList<>();
	private List<String> lastActiveDates = new ArrayList<>();

	public AdminView(List<Workgroup> workgroups, List<String> lastActiveDates) {
		setWorkgroups(workgroups);
		setLastActiveDates(lastActiveDates);
	}

	public List<Workgroup> getWorkgroups() {
		return workgroups;
	}

	public void setWorkgroups(List<Workgroup> workgroups) {
		this.workgroups = workgroups;
	}

	public List<String> getLastActiveDates() {
		return lastActiveDates;
	}

	public void setLastActiveDates(List<String> lastActiveDates) {
		this.lastActiveDates = lastActiveDates;
	}
}
