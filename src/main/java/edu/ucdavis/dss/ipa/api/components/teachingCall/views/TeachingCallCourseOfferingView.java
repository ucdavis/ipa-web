package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;

public class TeachingCallCourseOfferingView {
	private long id, seatsTotal;
	private String termCode, subjectCode, effectiveTermCode, courseNumber, title;
	private List<TeachingCallInstructorView> instructors = new ArrayList<TeachingCallInstructorView>();

	public TeachingCallCourseOfferingView(CourseOffering courseOffering) {
		if (courseOffering == null) return;
		setId(courseOffering.getId());
		setSeatsTotal(courseOffering.getSeatsTotal());
		setTermCode(courseOffering.getTermCode());
		setSubjectCode(courseOffering.getCourseOfferingGroup().getCourse().getSubjectCode());
		setEffectiveTermCode(courseOffering.getCourseOfferingGroup().getCourse().getEffectiveTermCode());
		setCourseNumber(courseOffering.getCourseOfferingGroup().getCourse().getCourseNumber());
		setTitle(courseOffering.getCourseOfferingGroup().getCourse().getTitle());
		setInstructors(courseOffering);
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

	public void setInstructors(CourseOffering courseOffering) {
		List<TeachingCallInstructorView> instructors = new ArrayList<TeachingCallInstructorView>();

		for (TeachingPreference teachingPreference : courseOffering.getTeachingPreferences()) {
			instructors.add(new TeachingCallInstructorView(teachingPreference.getInstructor()));
		}

		this.instructors = instructors;
	}
}
