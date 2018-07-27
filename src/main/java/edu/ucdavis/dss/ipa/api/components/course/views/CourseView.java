package edu.ucdavis.dss.ipa.api.components.course.views;

import edu.ucdavis.dss.ipa.entities.*;

import java.util.ArrayList;
import java.util.List;

public class CourseView {
	private List<Course> courses = new ArrayList<>();
	private List<SectionGroup> sectionGroups = new ArrayList<>();
	private List<Tag> tags = new ArrayList<>();
	private List<Term> terms = new ArrayList<>();
	private List<InstructorNote> instructorNotes = new ArrayList<>();

	public CourseView(
			List<Course> courses,
			List<SectionGroup> sectionGroups,
			List<Tag> tags,
			List<Term> terms,
			List<InstructorNote> instructorNotes) {

		setCourses(courses);
		setSectionGroups(sectionGroups);
		setTags(tags);
		setTerms(terms);
		setInstructorNotes(instructorNotes);
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

	public List<InstructorNote> getInstructorNotes() {
		return instructorNotes;
	}

	public void setInstructorNotes(List<InstructorNote> instructorNotes) {
		this.instructorNotes = instructorNotes;
	}
}
