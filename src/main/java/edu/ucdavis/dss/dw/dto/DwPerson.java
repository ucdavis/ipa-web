package edu.ucdavis.dss.dw.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwPerson {
	private String iamId, email, dFirstName, dMiddleName, dLastName, userId, oFirstName, oMiddleName, oLastName, dFullName, oFullName;

	public String getIamId() {
		return iamId;
	}

	public String getEmail() {
		return email;
	}

	public String getdFirstName() {
		return dFirstName;
	}

	public String getdMiddleName() {
		return dMiddleName;
	}

	public String getdLastName() {
		return dLastName;
	}

	public String getUserId() {
		return userId;
	}

	public String getoFirstName() {
		return oFirstName;
	}

	public String getoMiddleName() {
		return oMiddleName;
	}

	public String getoLastName() {
		return oLastName;
	}

	public String getdFullName() {
		return dFullName;
	}

	public String getoFullName() {
		return oFullName;
	}

	@Override
	public String toString() {
		return String.format("DwPerson[iamId=%s,userId=%s,oFullName=%s]", this.getEmail(), this.getUserId(), this.getoFullName());
	}
}