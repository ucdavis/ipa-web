package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwMeetingType {
	private char scheduleCode;

	public char getScheduleCode() {
		return scheduleCode;
	}
	public void setScheduleCode(char scheduleCode) {
		this.scheduleCode = scheduleCode;
	}

}
