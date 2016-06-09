package edu.ucdavis.dss.ipa.api.components.workgroup.views;

import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualCourseOfferingGroupView;
import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualInstructorView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.Track;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.List;

public class WorkgroupView {
	List<Track> tags = new ArrayList<Track>();
	List<UserRole> userRoles = new ArrayList<UserRole>();
	List<Role> roles = new ArrayList<Role>();
	List<WorkgroupUserView> users = new ArrayList<WorkgroupUserView>();

	public WorkgroupView(Workgroup workgroup, List<UserRole> userRoles, List<Role> roles, List<WorkgroupUserView> users) {
		setTags(workgroup.getActiveTracks());
		setUserRoles(userRoles);
		setRoles(roles);
		setUsers(users);
	}

	public List<Track> getTags() {
		return tags;
	}

	public void setTags(List<Track> tags) {
		this.tags = tags;
	}

	public List<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public List<WorkgroupUserView> getUsers() {
		return users;
	}

	public void setUsers(List<WorkgroupUserView> users) {
		this.users = users;
	}
}
