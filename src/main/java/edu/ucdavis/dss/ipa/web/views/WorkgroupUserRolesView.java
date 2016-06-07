package edu.ucdavis.dss.ipa.web.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;

public class WorkgroupUserRolesView {

	private List<WorkgroupUserView> senateUsers = new ArrayList<WorkgroupUserView>();
	private List<WorkgroupUserView> federationUsers = new ArrayList<WorkgroupUserView>();

	public WorkgroupUserRolesView(Workgroup workgroup) {
		setFederationUsers(workgroup);
		setSenateUsers(workgroup);
	}

	public List<WorkgroupUserView> getFederationUsers() {
		return this.federationUsers;
	}

	private void setFederationUsers(Workgroup workgroup) {
		List<WorkgroupUserView> federationUsers = new ArrayList<WorkgroupUserView>();

		for (UserRole userRole : workgroup.getUserRoles()) {
			if (userRole.getRoleToken().equals("federationInstructor")) {
				federationUsers.add(new WorkgroupUserView(userRole.getUser()));
			}
		}

		this.federationUsers = federationUsers;
	}

	public List<WorkgroupUserView> getSenateUsers() {
		return this.senateUsers;
	}

	private void setSenateUsers(Workgroup workgroup) {
		List<WorkgroupUserView> senateUsers = new ArrayList<WorkgroupUserView>();

		for (UserRole userRole : workgroup.getUserRoles()) {
			if (userRole.getRoleToken().equals("senateInstructor")) {
				senateUsers.add(new WorkgroupUserView(userRole.getUser()));
			}
		}
		this.senateUsers = senateUsers;
	}
}
