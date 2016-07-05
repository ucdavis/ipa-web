package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CourseView {
	List<Course> courses = new ArrayList<>();
	List<SectionGroup> sectionGroups = new ArrayList<>();
	List<ScheduleTermState> scheduleTermStates = new ArrayList<>();

	public CourseView(Schedule schedule, List<ScheduleTermState> scheduleTermStates) {
		setCourses(schedule.getCourses());
		setSectionGroups(schedule.getCourses());
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

	public void setSectionGroups(List<Course> courses) {
		for (Course course: courses) {
			for (SectionGroup sectionGroup: course.getSectionGroups()) {
				this.sectionGroups.add(sectionGroup);
			}
		}
	}

	public List<ScheduleTermState> getScheduleTermStates() {
		return scheduleTermStates;
	}

	public void setScheduleTermStates(List<ScheduleTermState> scheduleTermStates) {
		this.scheduleTermStates = scheduleTermStates;
	}
}
