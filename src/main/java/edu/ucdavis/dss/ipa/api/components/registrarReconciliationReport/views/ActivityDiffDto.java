package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views;


import org.javers.core.metamodel.annotation.DiffIgnore;

import jakarta.persistence.Id;

public class ActivityDiffDto {

	@DiffIgnore
	private long id;

	@Id
	private String uniqueKey;

	private char typeCode;
	private String bannerLocation, dayIndicator, startTime, endTime;

	public ActivityDiffDto(
			long activityId,
			char typeCode,
			String bannerLocation,
			String dayIndicator,
			String startTime,
			String endTime,
			String uniqueKey) {
		setId(activityId);
		setUniqueKey(uniqueKey);
		setTypeCode(typeCode);

		setBannerLocation(bannerLocation);
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

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public char getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(char typeCode) {
		this.typeCode = typeCode;
	}

	public String getBannerLocation() {
		return bannerLocation;
	}

	public void setBannerLocation(String bannerLocation) {
		this.bannerLocation = bannerLocation;
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
