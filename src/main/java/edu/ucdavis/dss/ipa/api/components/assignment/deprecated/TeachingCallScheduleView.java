package edu.ucdavis.dss.ipa.api.components.assignment.deprecated;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.TeachingCall;

public class TeachingCallScheduleView {
	private long id, year, workgroupId;
	private List<String> terms = new ArrayList<String>();
	private List<TeachingCall> teachingCalls = new ArrayList<TeachingCall>();
	private boolean isImporting;
	private List<ScheduleTermState> scheduleTermStates = new ArrayList<ScheduleTermState>();

	@JsonProperty("id")
	public long getId() {
		return this.id;
	}

	public void setId(long scheduleId) {
		this.id = scheduleId;
	}

	public long getYear() {
		return this.year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	@JsonProperty("terms")
	public List<String> getTerms() {
		return this.terms;
	}

	public void setTerms(List<String> summaryTermViews) {
		this.terms = summaryTermViews;
	}

	public boolean isImporting() {
		return isImporting;
	}

	public void setImporting(boolean isImporting) {
		this.isImporting = isImporting;
	}

	public List<TeachingCall> getTeachingCalls() {
		return teachingCalls;
	}

	public void setTeachingCalls(List<TeachingCall> teachingCalls) {
		this.teachingCalls = teachingCalls;
	}

	public List<ScheduleTermState> getScheduleTermStates() {
		return scheduleTermStates;
	}

	public void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
		this.scheduleTermStates = scheduleTermStates;
	}

	public long getWorkgroupId() {
		return this.workgroupId;
	}

	public void setWorkgroupId(long workgroupId) {
		this.workgroupId = workgroupId;
	}
}
