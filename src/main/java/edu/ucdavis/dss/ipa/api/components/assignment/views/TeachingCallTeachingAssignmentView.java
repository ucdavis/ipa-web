package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

/**
 * Used by InstructorController and CourseOfferingGroupsController
 * to return the instructors' preferences
 * Path: /api/schedules/{scheduleId}/teachingCallByInstructor
 * Path: /api/schedules/{scheduleId}/teachingCallByCourse
 *
 * @author okadri
 * 
 */
public class TeachingCallTeachingAssignmentView {
	private long id, priority;
	private TeachingCallSectionGroupView courseOffering;
	private String termCode;
	private Boolean isBuyout = false, isCourseRelease = false, IsSabbatical = false, approved = false;
	private TeachingCallInstructorView instructor;

	public TeachingCallTeachingAssignmentView(TeachingAssignment teachingAssignment) {
		setId(teachingAssignment.getId());
		setPriority(teachingAssignment.getPriority());
		setSectionGroup(teachingAssignment.getSectionGroup());
		setTermCode(teachingAssignment.getTermCode());
		setIsBuyout(teachingAssignment.isBuyout());
		setIsCourseRelease(teachingAssignment.isCourseRelease());
		setIsSabbatical(teachingAssignment.isSabbatical());
		setApproved(teachingAssignment.isApproved());
		setInstructor(teachingAssignment.getInstructor());
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

	public TeachingCallSectionGroupView getCourseOffering() {
		return courseOffering;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.courseOffering = new TeachingCallSectionGroupView(sectionGroup);
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
