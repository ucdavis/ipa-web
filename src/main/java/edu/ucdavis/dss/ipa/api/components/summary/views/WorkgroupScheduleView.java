package edu.ucdavis.dss.ipa.api.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.api.views.ScheduleViews;

/**
 * JSON DTO for /api/workgroups/{Id}/schedules
 * 
 * @author christopherthielen
 *
 */
public class WorkgroupScheduleView {
	long scheduleId, year, workgroupId;
	boolean isImporting;
	List<TeachingCall> teachingCalls = new ArrayList<TeachingCall>();
	List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();

	@JsonView(ScheduleViews.Summary.class)
	@JsonProperty("termStates")
	public List<ScheduleTermState> getScheduleTermStates() {
		return scheduleTermStates;
	}

	public void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
		this.scheduleTermStates = scheduleTermStates;
	}

	@JsonView(ScheduleViews.Summary.class)
	@JsonProperty("id")
	public long getScheduleId() {
		return this.scheduleId;
	}

	public void setScheduleId(long scheduleId) {
		this.scheduleId = scheduleId;
	}

	@JsonView(ScheduleViews.Summary.class)
	@JsonProperty("year")
	public long getYear() {
		return this.year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	@JsonView(ScheduleViews.Summary.class)
	@JsonProperty("isImporting")
	public boolean isImporting() {
		return isImporting;
	}

	public void setImporting(boolean isImporting) {
		this.isImporting = isImporting;
	}

	@JsonView(ScheduleViews.Summary.class)
	public List<TeachingCall> getTeachingCalls() {
		return teachingCalls;
	}

	public void setTeachingCalls(List<TeachingCall> teachingCalls) {
		this.teachingCalls = teachingCalls;
	}

	public void setWorkgroupId(long workgroupId) {
		this.workgroupId = workgroupId;
	}

	public long getWorkgroupId() {
		return this.workgroupId;
	}
}
