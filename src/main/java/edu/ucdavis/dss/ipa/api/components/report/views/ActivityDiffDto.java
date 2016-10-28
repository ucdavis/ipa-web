package edu.ucdavis.dss.ipa.api.components.report.views;


import javax.persistence.Id;

public class ActivityDiffDto {

	@Id
	private long id;

	private char typeCode;
	private String location, dayIndicator, startTime, endTime;

	public ActivityDiffDto(
			long activityId,
			char typeCode,
			String location,
			String dayIndicator,
			String startTime,
			String endTime) {
		setId(activityId);
		setTypeCode(typeCode);
		setLocation(location);
		setDayIndicator(dayIndicator);
		setStartTime(startTime);
		setEndTime(endTime);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public char getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(char typeCode) {
		this.typeCode = typeCode;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDayIndicator() {
		return dayIndicator;
	}

	public void setDayIndicator(String dayIndicator) {
		this.dayIndicator = dayIndicator;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
}
