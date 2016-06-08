package edu.ucdavis.dss.ipa.entities.enums;

public enum TermState {
	ANNUAL_DRAFT("Annual Draft"),
	INSTRUCTOR_CALL("Instructor Call"),
	TA_CALL("TA Call"),
	COMPLETED("Completed");

	private final String description;

	TermState(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}
}
