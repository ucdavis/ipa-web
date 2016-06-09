package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;
import edu.ucdavis.dss.ipa.entities.Term;

/**
 * Used by InstructorController to return the list of instructors and their preferences
 * for a given schedule
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 *
 * @author okadri
 * 
 */
public class TeachingCallByInstructorView {
	private String firstName, lastName;
	private long id;
	private HashMap<String,List<TeachingCallTeachingPreferenceView>> teachingPreferences = new HashMap<String,List<TeachingCallTeachingPreferenceView>>();
	private HashMap<String,TeachingCallTeachingCallResponseView> teachingCallResponses = new HashMap<String,TeachingCallTeachingCallResponseView>();
	private TeachingCallTeachingCallReceiptView teachingCallReceipt;
	private TeachingCallScheduleInstructorNoteView scheduleInstructorNote;

	public TeachingCallByInstructorView(
			Instructor instructor,
			List<TeachingPreference> scheduleTeachingPreferences,
			List<TeachingCallResponse> scheduleTeachingCallResponses,
			TeachingCallReceipt teachingCallReceipt,
			ScheduleInstructorNote scheduleInstructorNote,
			long year) {
		setId(instructor.getId());
		setFirstName(instructor.getFirstName());
		setLastName(instructor.getLastName());
		setTeachingPreferences(scheduleTeachingPreferences, year);
		setTeachingCallResponses(scheduleTeachingCallResponses);
		setTeachingCallReceipt(teachingCallReceipt);
		setScheduleInstructorNote(scheduleInstructorNote);
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public HashMap<String,List<TeachingCallTeachingPreferenceView>> getTeachingPreferences() {
		return teachingPreferences;
	}

	public void setTeachingPreferences(List<TeachingPreference> scheduleTeachingPreferences, long year) {
		// Initialize teachingPreferences for all term of the year
		for (String termCode: Term.getTermCodesByYear(year)) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(termCode);
			this.teachingPreferences.put(term, new ArrayList<TeachingCallTeachingPreferenceView>());
		}
		for (TeachingPreference teachingPreference: scheduleTeachingPreferences) {
			// Get the 2 digit termCode
			String term = Term.getTwoDigitTermCode(teachingPreference.getTermCode());

			// Append to teachingPreferences of that term
			List<TeachingCallTeachingPreferenceView> termTeachingPreferences = this.teachingPreferences.get(term);
			termTeachingPreferences.add(new TeachingCallTeachingPreferenceView(teachingPreference));
		}
	}

	public HashMap<String,TeachingCallTeachingCallResponseView> getTeachingCallResponses() {
		return teachingCallResponses;
	}

	public void setTeachingCallResponses(List<TeachingCallResponse> schedulTeachingCallResponses) {
		for (TeachingCallResponse teachingCallResponse: schedulTeachingCallResponses) {
			// Get the 2 digit termCode
			String termCode = teachingCallResponse.getTermCode().substring(Math.max(teachingCallResponse.getTermCode().length() - 2, 0));

			// Set the teachingCallResponses for the termCode
			this.teachingCallResponses.put(termCode, new TeachingCallTeachingCallResponseView(teachingCallResponse));
		}
	}

	public TeachingCallTeachingCallReceiptView getTeachingCallReceipt() {
		return teachingCallReceipt;
	}

	public void setTeachingCallReceipt(TeachingCallReceipt teachingCallReceipt) {
		this.teachingCallReceipt = new TeachingCallTeachingCallReceiptView(teachingCallReceipt);
	}

	public TeachingCallScheduleInstructorNoteView getScheduleInstructorNote() {
		return scheduleInstructorNote;
	}

	public void setScheduleInstructorNote(ScheduleInstructorNote scheduleInstructorNote) {
		this.scheduleInstructorNote = new TeachingCallScheduleInstructorNoteView(scheduleInstructorNote);
	}

}
