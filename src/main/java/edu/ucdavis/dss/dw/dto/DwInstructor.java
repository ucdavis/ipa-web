package edu.ucdavis.dss.dw.dto;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwInstructor {
	private long id;
	private String firstName, middleInitial, lastName, emailAddress, employeeId, loginId;

	@Id
	public long getId() {
		return id;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getMiddleInitial() {
		return middleInitial;
	}

	public String getLastName() {
		return lastName;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	@Override
	public String toString() {
		return String.format("[DwInstructor id: %d, first: %s, middle: %s, last: %s,"
				+ "email: %s, employeeId: %s, loginId: %s", id, firstName, middleInitial, lastName, emailAddress, employeeId, loginId);
	}

	public String getLoginId() {
		return loginId;
	}
}
