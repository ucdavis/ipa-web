package edu.ucdavis.dss.ipa.api.components.workgroup.views;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class WorkgroupView {
	private List<Tag> tags = new ArrayList<Tag>();
	private List<Role> roles = new ArrayList<Role>();
	private List<User> users = new ArrayList<User>();
	private List<Location> locations = new ArrayList<Location>();
	private String workgroupName;


	public WorkgroupView(Workgroup workgroup, List<UserRole> userRoles, List<Role> roles, List<User> users) {
		setTags(workgroup.getTags());
		setRoles(roles);
		setUsers(users);
		setLocations(workgroup.getLocations());
		setWorkgroupName(workgroup.getName());
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public String getWorkgroupName() {
		return workgroupName;
	}

	public void setWorkgroupName(String workgroupName) {
		this.workgroupName = workgroupName;
	}
}
