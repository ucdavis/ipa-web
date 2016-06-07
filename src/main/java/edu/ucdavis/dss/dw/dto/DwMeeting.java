package edu.ucdavis.dss.dw.dto;

import java.sql.Time;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwMeeting {
	private Time beginTime, endTime;
	private String buildingCode, roomCode;
	private Date startDate, endDate;
	private boolean sundayIndicator, mondayIndicator, tuesdayIndicator, wednesdayIndicator, thursdayIndicator, fridayIndicator, saturdayIndicator;
	private DwMeetingType scheduleCode;
	private int totalMeetings;

	public String getBuildingCode() {
		return buildingCode;
	}

	public void setBuildingCode(String buildingCode) {
		this.buildingCode = buildingCode;
	}

	public String getRoomCode() {
		return roomCode;
	}

	public void setRoomCode(String roomCode) {
		this.roomCode = roomCode;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public boolean isSundayIndicator() {
		return sundayIndicator;
	}

	public void setSundayIndicator(boolean sundayIndicator) {
		this.sundayIndicator = sundayIndicator;
	}

	public boolean isMondayIndicator() {
		return mondayIndicator;
	}

	public void setMondayIndicator(boolean mondayIndicator) {
		this.mondayIndicator = mondayIndicator;
	}

	public boolean isTuesdayIndicator() {
		return tuesdayIndicator;
	}

	public void setTuesdayIndicator(boolean tuesdayIndicator) {
		this.tuesdayIndicator = tuesdayIndicator;
	}

	public boolean isWednesdayIndicator() {
		return wednesdayIndicator;
	}

	public void setWednesdayIndicator(boolean wednesdayIndicator) {
		this.wednesdayIndicator = wednesdayIndicator;
	}

	public boolean isThursdayIndicator() {
		return thursdayIndicator;
	}

	public void setThursdayIndicator(boolean thursdayIndicator) {
		this.thursdayIndicator = thursdayIndicator;
	}

	public boolean isFridayIndicator() {
		return fridayIndicator;
	}

	public void setFridayIndicator(boolean fridayIndicator) {
		this.fridayIndicator = fridayIndicator;
	}

	public boolean isSaturdayIndicator() {
		return saturdayIndicator;
	}

	public void setSaturdayIndicator(boolean saturdayIndicator) {
		this.saturdayIndicator = saturdayIndicator;
	}

	public DwMeetingType getScheduleCode() {
		return scheduleCode;
	}

	public void setScheduleCode(DwMeetingType scheduleCode) {
		this.scheduleCode = scheduleCode;
	}

	public int getTotalMeetings() {
		return totalMeetings;
	}

	public void setTotalMeetings(int totalMeetings) {
		this.totalMeetings = totalMeetings;
	}

	public Time getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(Time beginTime) {
		this.beginTime = beginTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	/**
	 * @return A 7-character string representing the day indicators, e.g. 0000010 (only Friday indicated)
	 */
	public String getDayIndicator() {
		char[] days = new char[7];

		days[0] = this.isSundayIndicator() ? '1' : '0';
		days[1] = this.isMondayIndicator() ? '1' : '0';
		days[2] = this.isTuesdayIndicator() ? '1' : '0';
		days[3] = this.isWednesdayIndicator() ? '1' : '0';
		days[4] = this.isThursdayIndicator() ? '1' : '0';
		days[5] = this.isFridayIndicator() ? '1' : '0';
		days[6] = this.isSaturdayIndicator() ? '1' : '0';

		return new String(days);
	}

	/**
	 * @return The number of days indicated by the weekday indicators.
	 */
	public int getDaysIndicated() {
		return StringUtils.countMatches(this.getDayIndicator(), "1");
	}
}
