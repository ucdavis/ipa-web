package edu.ucdavis.dss.ipa.api.components.teachingCall.views;

import java.util.ArrayList;
import java.util.List;

public class TeachingCallTermUnscheduledCoursesView {
	private List<Course> courses = new ArrayList<Course>();
	private String termCode;

	public TeachingCallTermUnscheduledCoursesView (List<Course> courses, String termCode) {
		this.setTermCode(termCode);
		this.setCourses(courses);
	}

	public String getTermCode() {
		return this.termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public List<Course> getCourses() {
		return this.courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}
}
