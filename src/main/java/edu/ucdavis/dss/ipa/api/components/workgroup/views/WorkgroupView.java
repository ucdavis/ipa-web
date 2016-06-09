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

	public WorkgroupView(Workgroup workgroup, List<UserRole> userRoles) {
		setTags(workgroup.getActiveTracks());
		setUserRoles(userRoles);
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

}
