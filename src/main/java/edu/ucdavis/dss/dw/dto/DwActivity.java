package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwActivity {
	private String day_indicator, ssrmeet_begin_time, ssrmeet_end_time, ssrmeet_room_code, ssrmeet_bldg_code;
	private char ssrmeet_schd_code;

	public String getDay_indicator() {
		return day_indicator;
	}

	public void setDay_indicator(String day_indicator) {
		this.day_indicator = day_indicator;
	}

	public String getSsrmeet_begin_time() {
		return ssrmeet_begin_time;
	}

	public void setSsrmeet_begin_time(String ssrmeet_begin_time) {
		this.ssrmeet_begin_time = ssrmeet_begin_time;
	}

	public String getSsrmeet_end_time() {
		return ssrmeet_end_time;
	}

	public void setSsrmeet_end_time(String ssrmeet_end_time) {
		this.ssrmeet_end_time = ssrmeet_end_time;
	}

	public String getSsrmeet_room_code() {
		return ssrmeet_room_code;
	}

	public void setSsrmeet_room_code(String ssrmeet_room_code) {
		this.ssrmeet_room_code = ssrmeet_room_code;
	}

	public String getSsrmeet_bldg_code() {
		return ssrmeet_bldg_code;
	}

	public void setSsrmeet_bldg_code(String ssrmeet_bldg_code) {
		this.ssrmeet_bldg_code = ssrmeet_bldg_code;
	}

	public char getSsrmeet_schd_code() {
		return ssrmeet_schd_code;
	}

	public void setSsrmeet_schd_code(char ssrmeet_schd_code) {
		this.ssrmeet_schd_code = ssrmeet_schd_code;
	}
}