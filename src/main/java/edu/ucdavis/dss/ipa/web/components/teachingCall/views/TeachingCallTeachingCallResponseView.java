package edu.ucdavis.dss.ipa.web.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;

/**
 * Used by InstructorController to return the list of instructors and their preferences
 * for a given schedule
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 *
 * @author okadri
 * 
 */
public class TeachingCallTeachingCallResponseView {

	private String availabilityBlob, termCode;

	public TeachingCallTeachingCallResponseView(TeachingCallResponse teachingCallResponse) {
		setAvailabilityBlob(teachingCallResponse.getAvailabilityBlob());
		setTermCode(teachingCallResponse.getTermCode());
	}

	public String getAvailabilityBlob() {
		return availabilityBlob;
	}

	public void setAvailabilityBlob(String availabilityBlob) {
		this.availabilityBlob = availabilityBlob;
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

}
