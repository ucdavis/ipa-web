package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views;


import jakarta.persistence.Id;

public class InstructorDiffDto {

	@Id
	private String uniqueKey;	// Can be customized to any unique key we end up using (currently loginId)

	private String firstName, lastName, loginId, ucdStudentSID;

	public InstructorDiffDto(
			String firstName,
			String lastName,
			String loginId,
			String ucdStudentSID
	) {
		setUniqueKey(loginId.toLowerCase());
		setFirstName(firstName);
		setLastName(lastName);
		setLoginId(loginId);
		setUcdStudentSID(ucdStudentSID);
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getUcdStudentSID() {
		return ucdStudentSID;
	}

	public void setUcdStudentSID(String ucdStudentSID) {
		this.ucdStudentSID = ucdStudentSID;
	}
}
