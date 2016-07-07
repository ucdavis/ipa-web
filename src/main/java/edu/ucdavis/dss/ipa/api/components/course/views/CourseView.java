package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CourseView {
	private List<Course> courses = new ArrayList<>();
	private List<SectionGroup> sectionGroups = new ArrayList<>();
	private List<ScheduleTermState> scheduleTermStates = new ArrayList<>();

	public CourseView(List<Course> courses, List<SectionGroup> sectionGroups, List<ScheduleTermState> scheduleTermStates) {
		setCourses(courses);
		setSectionGroups(sectionGroups);
		setScheduleTermStates(scheduleTermStates);
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	public List<SectionGroup> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
		this.sectionGroups = sectionGroups;
	}

	public List<ScheduleTermState> getScheduleTermStates() {
		return scheduleTermStates;
	}

	public void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
		this.scheduleTermStates = scheduleTermStates;
	}
}
