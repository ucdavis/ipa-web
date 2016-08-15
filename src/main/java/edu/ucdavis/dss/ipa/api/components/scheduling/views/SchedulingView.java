package edu.ucdavis.dss.ipa.api.components.scheduling.views;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Tag;

import java.util.ArrayList;
import java.util.List;

public class SchedulingView {
	private List<SectionGroup> sectionGroups = new ArrayList<>();
	private List<Course> courses = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();

	public SchedulingView(List<Course> courses, List<SectionGroup> sectionGroups, List<Tag> tags) {
		setSectionGroups(sectionGroups);
		setCourses(courses);
		setTags(tags);
	}

	public List<SectionGroup> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
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
}
