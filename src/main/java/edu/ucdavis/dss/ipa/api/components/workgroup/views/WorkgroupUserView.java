package edu.ucdavis.dss.ipa.api.components.workgroup.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class WorkgroupUserView {
	long id;
	String name, loginId;
	List<Long> userRoleIds = new ArrayList<Long>();

	public WorkgroupUserView(User user) {
		setId(user.getId());
		setName(user.getName());
		setLoginId(user.getLoginId());
		setUserRoleIds(user.getUserRoles());
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

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public List<Long> getUserRoleIds() {
		return userRoleIds;
	}

	public void setUserRoleIds(List<UserRole> userRoles) {
		for (UserRole userRole: userRoles) {
			this.userRoleIds.add(userRole.getId());
		}
	}
}
