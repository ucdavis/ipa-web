package edu.ucdavis.dss.ipa.api.components.assignment.deprecated;

import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;

/**
 * Used by InstructorController to return the list of instructors and their preferences
 * for a given schedule
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 *
 * @author okadri
 * 
 */
public class TeachingCallScheduleInstructorNoteView {

	private long id;
	private boolean assignmentsCompleted;

	public TeachingCallScheduleInstructorNoteView(ScheduleInstructorNote scheduleInstructorNote) {
		if (scheduleInstructorNote == null) return;
		setId(scheduleInstructorNote.getId());
		setAssignmentsCompleted(scheduleInstructorNote.getAssignmentsCompleted());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isAssignmentsCompleted() {
		return assignmentsCompleted;
	}

	public void setAssignmentsCompleted(boolean assignmentsCompleted) {
		this.assignmentsCompleted = assignmentsCompleted;
	}

}
