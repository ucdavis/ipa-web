package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

import java.util.ArrayList;
import java.util.List;

public class TeachingCallSectionGroupView {
	private long id, seatsTotal;
	private String termCode, subjectCode, effectiveTermCode, courseNumber, title;
	private List<TeachingCallInstructorView> instructors = new ArrayList<TeachingCallInstructorView>();

	public TeachingCallSectionGroupView(SectionGroup sectionGroup) {
		if (sectionGroup == null) return;
		setId(sectionGroup.getId());
		setSeatsTotal(sectionGroup.getPlannedSeats());
		setTermCode(sectionGroup.getTermCode());
		setSubjectCode(sectionGroup.getCourse().getSubjectCode());
		setEffectiveTermCode(sectionGroup.getCourse().getEffectiveTermCode());
		setCourseNumber(sectionGroup.getCourse().getCourseNumber());
		setTitle(sectionGroup.getCourse().getTitle());
		setInstructors(sectionGroup);
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public void setEffectiveTermCode(String effectiveTermCode) {
		this.effectiveTermCode = effectiveTermCode;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSeatsTotal() {
		return seatsTotal;
	}

	public void setSeatsTotal(long seatsTotal) {
		this.seatsTotal = seatsTotal;
	}

	public List<TeachingCallInstructorView> getInstructors() {
		return this.instructors;
	}

	public void setInstructors(SectionGroup sectionGroup) {
		List<TeachingCallInstructorView> instructors = new ArrayList<TeachingCallInstructorView>();

		for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
			instructors.add(new TeachingCallInstructorView(teachingAssignment.getInstructor()));
		}

		this.instructors = instructors;
	}
}
