package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.sql.Time;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwActivity {
	private String day_indicator, ssrmeet_begin_time, ssrmeet_end_time, ssrmeet_room_code, ssrmeet_bldg_code, catagory;
	private char ssrmeet_schd_code;

	public String getCatagory() {
		return catagory;
	}

	public void setCatagory(String catagory) {
		this.catagory = catagory;
	}

	public String getDay_indicator() {
		return day_indicator;
	}
	public void setDay_indicator(String day_indicator) {
		this.day_indicator = day_indicator;
	}

	public String getSsrmeet_begin_time() { return ssrmeet_begin_time; }
	public void setSsrmeet_begin_time(String ssrmeet_begin_time) {
		this.ssrmeet_begin_time = ssrmeet_begin_time;
	}

	/**
	 * Returns the value "ssrmeet_begin_time()" casted to Java's Time object
	 */
	public Time castBeginTime() {
		String rawStartTime = this.getSsrmeet_begin_time();

		if (rawStartTime == null) { return null; }

		String hours = rawStartTime.substring(0, 2);
		String minutes = rawStartTime.substring(2, 4);

		return java.sql.Time.valueOf(hours + ":" + minutes + ":00");
	}

	public String getSsrmeet_end_time() {
		return ssrmeet_end_time;
	}
	public void setSsrmeet_end_time(String ssrmeet_end_time) {
		this.ssrmeet_end_time = ssrmeet_end_time;
	}

	/**
	 * Returns the value "ssrmeet_end_time()" casted to Java's Time object
	 */
	public Time castEndTime() {
		String rawEndTime = this.getSsrmeet_end_time();

		if (rawEndTime == null) { return null; }

		String hours = rawEndTime.substring(0, 2);
		String minutes = rawEndTime.substring(2, 4);

		return java.sql.Time.valueOf(hours + ":" + minutes + ":00");
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

	public char getSsrmeet_schd_code() { return ssrmeet_schd_code; }
	public void setSsrmeet_schd_code(char ssrmeet_schd_code) { this.ssrmeet_schd_code = ssrmeet_schd_code; }

	// Generates a unique key per activity. Useful during the import process.
	public String getActivitySortingKey(String sectionSortingKey) {
		String startTime = this.getSsrmeet_begin_time() != null ? this.getSsrmeet_begin_time() : "";
		String endTime = this.getSsrmeet_end_time() != null ? this.getSsrmeet_end_time() : "";
		String activityType = String.valueOf(this.getSsrmeet_schd_code());
		String dayIndicator = this.getDay_indicator() != null ? this.getDay_indicator() : "";

		return sectionSortingKey + "-" + activityType + "-" + dayIndicator + "-" + startTime + "-" + endTime;
	}


	@Override
	public String toString() {
		return String.format("Day indicator: " + day_indicator + "," +
				"begin time: " + ssrmeet_begin_time + "," +
				"end time: " + ssrmeet_end_time + "," +
				"room code: " + ssrmeet_room_code + "," +
				"bldg code: " + ssrmeet_bldg_code + "," +
				"schd code: " + ssrmeet_schd_code);
	}
}