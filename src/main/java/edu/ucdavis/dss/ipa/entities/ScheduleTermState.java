package edu.ucdavis.dss.ipa.entities;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.enums.TermState;
import edu.ucdavis.dss.ipa.web.views.ScheduleViews;
import edu.ucdavis.dss.ipa.web.views.WorkgroupViews;

public class ScheduleTermState {
	private String termCode;
	private TermState state;

	@JsonProperty("isLocked")
	@JsonView(ScheduleViews.Summary.class)
	public boolean scheduleTermLocked() {
		if (state != null) {
			switch(state) {
			case COMPLETED:
				return true;
			case ANNUAL_DRAFT: case INSTRUCTOR_CALL: case TA_CALL:
				return false;
			}
			return false;
		} else {
			return false;
		}
	}

	@JsonProperty("termCode")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@JsonProperty("state")
	public TermState getState() {
		return this.state;
	}

	public void setState(TermState state) {
		this.state = state;
	}

	@Transient
	@JsonProperty("stateOrdinal")
	@JsonView(ScheduleViews.Summary.class)
	public Integer getStateOrdinal() {
		if(this.state != null) {
			return this.state.ordinal();
		}
		else {
			return -1;
		}
	}
}