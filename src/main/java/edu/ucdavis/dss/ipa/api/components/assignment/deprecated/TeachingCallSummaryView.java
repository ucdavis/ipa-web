package edu.ucdavis.dss.ipa.api.components.assignment.deprecated;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.TeachingCall;

public class TeachingCallSummaryView {
	private long id, scheduleId, year;
	private boolean showUnavailabilities;
	private List<String> terms = new ArrayList<String>();
	private String termsBlob;

	public TeachingCallSummaryView(TeachingCall teachingCall, List<String> terms) {
		setId(teachingCall.getId());
		setShowUnavailabilities(teachingCall.isShowUnavailabilities());
		setScheduleId(teachingCall.getSchedule().getId());
		setYear(teachingCall.getSchedule().getYear());
		setTerms(terms);
		setTermsBlob(teachingCall.getTermsBlob());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(long scheduleId) {
		this.scheduleId = scheduleId;
	}

	public boolean isShowUnavailabilities() {
		return showUnavailabilities;
	}

	public void setShowUnavailabilities(boolean showUnavailabilities) {
		this.showUnavailabilities = showUnavailabilities;
	}

	public List<String> getTerms() {
		return terms;
	}

	public void setTerms(List<String> terms) {
		this.terms = terms;
	}

	public long getYear() {
		return year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	public String getTermsBlob() {
		return this.termsBlob;
	}

	public void setTermsBlob(String termsBlob) {
		this.termsBlob = termsBlob;
	}
}
