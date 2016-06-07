package edu.ucdavis.dss.ipa.enums;

/**
 *
 * @author okadri
 * Draft: Initial state
 * Scheduled: Indicated that the AC has completed scheduling an activity.
 *     Was added because some activities from DW have NULL startTime and endTime
 *     and we needed something to indicate which activities can be considered scheduled
 * Submitted: To Registrar's Office
 * Confirmed: By Registrar's Office
 *
 */

public enum ActivityState {
	DRAFT("Draft"),
	SUBMITTED("Submitted"),
	CONFIRMED("Confirmed");

	private final String description;

	ActivityState(String description) {
		this.description = description;
	}

	public String getDescription() {
		return this.description;
	}

}
