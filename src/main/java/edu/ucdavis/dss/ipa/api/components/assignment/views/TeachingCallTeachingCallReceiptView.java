package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;

/**
 * Used by InstructorController to return the list of instructors and their preferences
 * for a given schedule
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 *
 * @author okadri
 * 
 */
public class TeachingCallTeachingCallReceiptView {

	private long id, teachingCallId;
	private boolean isDone;
	private String comment;

	public TeachingCallTeachingCallReceiptView(TeachingCallReceipt teachingCallReceipt) {
		if (teachingCallReceipt == null) return;
		setId(teachingCallReceipt.getId());
		setDone(teachingCallReceipt.getIsDone());
		setComment(teachingCallReceipt.getComment());
		setTeachingCallId(teachingCallReceipt.getTeachingCall().getId());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public long getTeachingCallId() {
		return teachingCallId;
	}

	public void setTeachingCallId(long teachingCallId) {
		this.teachingCallId = teachingCallId;
	}

}
