package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import edu.ucdavis.dss.ipa.entities.Instructor;

/**
 * Used by InstructorController and CourseOfferingGroupsController
 * to return the instructors' preferences
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 * Path: /api/schedules/{scheduleId}/teachingCallByCourse
 *
 * @author okadri
 * 
 */
public class TeachingCallTeachingPreferenceView {
	private long id, priority;
	private TeachingCallCourseOfferingView courseOffering;
	private Course course;
	private String notes, termCode;	
	private Boolean isBuyout = false, isCourseRelease = false, IsSabbatical = false, approved = false;
	private TeachingCallInstructorView instructor;

	public TeachingCallTeachingPreferenceView(TeachingPreference teachingPreference) {
		setId(teachingPreference.getId());
		setPriority(teachingPreference.getPriority());
		setCourseOffering(teachingPreference.getCourseOffering());
		setNotes(teachingPreference.getNotes());
		setTermCode(teachingPreference.getTermCode());
		setIsBuyout(teachingPreference.getIsBuyout());
		setIsCourseRelease(teachingPreference.getIsCourseRelease());
		setIsSabbatical(teachingPreference.getIsSabbatical());
		setApproved(teachingPreference.isApproved());
		setInstructor(teachingPreference.getInstructor());
		setCourse(teachingPreference.getCourse());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public TeachingCallCourseOfferingView getCourseOffering() {
		return courseOffering;
	}

	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = new TeachingCallCourseOfferingView(courseOffering);
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public Boolean getIsBuyout() {
		return isBuyout;
	}

	public void setIsBuyout(Boolean isBuyout) {
		this.isBuyout = isBuyout;
	}

	public Boolean getIsCourseRelease() {
		return isCourseRelease;
	}

	public void setIsCourseRelease(Boolean isCourseRelease) {
		this.isCourseRelease = isCourseRelease;
	}

	public Boolean getIsSabbatical() {
		return IsSabbatical;
	}

	public void setIsSabbatical(Boolean isSabbatical) {
		IsSabbatical = isSabbatical;
	}

	public Boolean getApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}

	public TeachingCallInstructorView getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor teachingCallInstructor) {
		this.instructor = new TeachingCallInstructorView(teachingCallInstructor);
	}

}
