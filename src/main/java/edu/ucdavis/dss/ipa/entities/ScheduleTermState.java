package edu.ucdavis.dss.ipa.entities;

import jakarta.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.entities.enums.TermState;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleTermState {
	private String termCode;
	private TermState state;

	@Transient
	@JsonProperty("isLocked")
	public boolean scheduleTermLocked() {
		if (state != null) {
			switch(state) {
			case COMPLETED:
				return true;
			case ANNUAL_DRAFT:
				return false;
			}
			return false;
		} else {
			return false;
		}
	}

	@JsonProperty("termCode")
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
	public Integer getStateOrdinal() {
		if(this.state != null) {
			return this.state.ordinal();
		}
		else {
			return -1;
		}
	}
}
