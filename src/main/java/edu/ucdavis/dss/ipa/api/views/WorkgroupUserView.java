package edu.ucdavis.dss.ipa.api.views;

import edu.ucdavis.dss.ipa.entities.User;

public class WorkgroupUserView {

	private String loginId, email, firstName, lastName;

	public WorkgroupUserView(User user) {
		setFirstName(user);
		setLastName(user);
		setLoginId(user);
		setEmail(user);
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(User user) {
		this.loginId = user.getLoginId();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(User user) {
		this.email = user.getEmail();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(User user) {
		this.firstName = user.getFirstName();
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(User user) {
		this.lastName = user.getLastName();
	}

}
