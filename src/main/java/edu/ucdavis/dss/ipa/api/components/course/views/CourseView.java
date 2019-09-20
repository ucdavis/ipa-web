package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CourseView {
	private List<Course> courses = new ArrayList<>();
	private List<SectionGroup> sectionGroups = new ArrayList<>();
	private List<Section> sections = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();
	private List<Term> terms = new ArrayList<>();

	public CourseView(
			List<Course> courses,
			List<SectionGroup> sectionGroups,
			List<Section> sections,
			List<Tag> tags,
			List<Term> terms) {

		setCourses(courses);
		setSectionGroups(sectionGroups);
		setSections(sections);
		setTags(tags);
		setTerms(terms);
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

	public List<Section> getSections() { return sections; }

	public void setSections(List<Section> sections) { this.sections = sections; }

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public List<Term> getTerms() {
		return terms;
	}

	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}
}
