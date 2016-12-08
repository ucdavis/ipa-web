package edu.ucdavis.dss.ipa.api.components.scheduling.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class SchedulingView {
	private List<SchedulingViewSectionGroup> sectionGroups = new ArrayList<>();
	private List<Course> courses = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();
	private List<Location> locations = new ArrayList<>();
	private List<Instructor> instructors = new ArrayList<>();
	private List<Activity> activities = new ArrayList<>();
	private Term term;
	private List<Section> sections;
	private List<TeachingCallResponse> teachingCallResponses;
	private List<TeachingAssignment> teachingAssignments;


	public SchedulingView(
			List<Course> courses,
			List<SchedulingViewSectionGroup> sectionGroups,
			List<Tag> tags,
			List<Location> locations,
			List<Instructor> instructors,
			List<Activity> activities,
			Term term,
			List<Section> sections,
			List<TeachingCallResponse> teachingCallResponses,
			List<TeachingAssignment> teachingAssignments
	) {
		setSectionGroups(sectionGroups);
		setCourses(courses);
		setTags(tags);
		setLocations(locations);
		setInstructors(instructors);
		setActivities(activities);
		setTerm(term);
		setSections(sections);
		setTeachingCallResponses(teachingCallResponses);
		setTeachingAssignments(teachingAssignments);
	}

	public List<SchedulingViewSectionGroup> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SchedulingViewSectionGroup> sectionGroups) {
		this.sectionGroups = sectionGroups;
	}

	public List<Course> getCourses() {
		return courses;
	}

	public void setCourses(List<Course> courses) {
		this.courses = courses;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

	public List<Instructor> getInstructors() {
		return instructors;
	}

	public void setInstructors(List<Instructor> instructors) {
		this.instructors = instructors;
	}

	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	public Term getTerm() {
		return term;
	}

	public void setTerm(Term term) {
		this.term = term;
	}

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public List<TeachingCallResponse> getTeachingCallResponses() {
		return teachingCallResponses;
	}

	public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
		this.teachingCallResponses = teachingCallResponses;
	}

	public List<TeachingAssignment> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		this.teachingAssignments = teachingAssignments;
	}
}
