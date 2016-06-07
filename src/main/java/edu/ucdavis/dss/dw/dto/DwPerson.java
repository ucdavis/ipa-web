package edu.ucdavis.dss.dw.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwPerson {
	private String email, first, middle, last, loginId;
	private List<DwAppointment> appointments;
	
	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getMiddle() {
		return middle;
	}

	public void setMiddle(String middle) {
		this.middle = middle;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public String getEmail() {
		return email;
	}
	
	public String getLoginId() {
		return loginId;
	}
	
	public List<DwAppointment> getAppointments() {
		return appointments;
	}
	
	@Override
	public String toString() {
		return String.format("DwPerson[email=%s,first=%s,last=%s,loginId=%s]", this.getEmail(), this.getFirst(), this.getLast(), this.getLoginId());
	}
}