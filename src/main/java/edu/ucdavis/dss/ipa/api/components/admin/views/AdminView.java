package edu.ucdavis.dss.ipa.api.components.admin.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class AdminView {
	private List<Workgroup> workgroups = new ArrayList<>();

	public AdminView(List<Workgroup> workgroups) {
		setWorkgroups(workgroups);
	}

	public List<Workgroup> getWorkgroups() {
		return workgroups;
	}

	public void setWorkgroups(List<Workgroup> workgroups) {
		this.workgroups = workgroups;
	}
}
