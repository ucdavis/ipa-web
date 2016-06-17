package edu.ucdavis.dss.ipa.api.components.workgroup.views;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class WorkgroupView {
	List<Tag> tags = new ArrayList<Tag>();
	List<Role> roles = new ArrayList<Role>();
	List<User> users = new ArrayList<User>();
	List<Location> locations = new ArrayList<Location>();

	public WorkgroupView(Workgroup workgroup, List<UserRole> userRoles, List<Role> roles, List<User> users) {
		setTags(workgroup.getTags());
		setRoles(roles);
		setUsers(users);
		setLocations(workgroup.getLocations());
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
}
