package edu.ucdavis.dss.ipa.api.components.term.views;

import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.ipa.entities.*;

public class TermCourseOfferingView {
	private long id, seatsTotal, enrollmentTotal;
	private String termCode, subjectCode, courseNumber, title;
	private List<TermSectionGroupView> sectionGroups = new ArrayList<TermSectionGroupView>();
	private List<TermTrackView> tracks = new ArrayList<TermTrackView>();
	private List<TermInstructorView> instructors = new ArrayList<TermInstructorView>();

	public TermCourseOfferingView(CourseOffering courseOffering) {
		if (courseOffering == null) return;
		setTermCode(courseOffering.getTermCode());
		setCourseNumber(courseOffering.getCourse().getCourse().getCourseNumber());
		setId(courseOffering.getId());
		setSubjectCode(courseOffering.getCourse().getCourse().getSubjectCode());
		setTitle(courseOffering.getCourse().getTitle());
		setSeatsTotal(courseOffering.getSeatsTotal());
		setEnrollmentTotal(courseOffering);
		setSectionGroups(courseOffering.getSectionGroups());
		setTracks(courseOffering.getCourse().getTags());
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

	public long getEnrollmentTotal() {
		return enrollmentTotal;
	}

	public void setSeatsTotal(long seatsTotal) {
		this.seatsTotal = seatsTotal;
	}

	public List<TermSectionGroupView> getSectionGroups() {
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
		for (SectionGroup sectionGroup : sectionGroups) {
			this.sectionGroups.add(new TermSectionGroupView(sectionGroup));
		}
	}

	public List<TermTrackView> getTracks() {
		return tracks;
	}

	public void setTracks(List<Tag> tags) {
		for (Tag tag : tags) {
			this.tracks.add(new TermTrackView(tag));
		}
	}

	public List<TermInstructorView> getInstructors() {
		return instructors;
	}

	public void setInstructors(CourseOffering courseOffering) {
		List<Instructor> coInstructors = new ArrayList<Instructor>();
		// Instructors that are assigned
		boolean IS_ASSIGNED = true;
		for (SectionGroup sectionGroup: courseOffering.getSectionGroups()) {
			for (TeachingAssignment ta: sectionGroup.getTeachingAssignments()) {
				if (coInstructors.contains(ta.getInstructor()) == false) {
					coInstructors.add(ta.getInstructor());
					this.instructors.add(new TermInstructorView(ta.getInstructor(), IS_ASSIGNED));
				}
			}
		}
		// Instructors that are approved but not assigned
		IS_ASSIGNED = false;
		for (TeachingPreference tp: courseOffering.getTeachingPreferences()) {
			if (tp.isApproved()) {
				if (coInstructors.contains(tp.getInstructor()) == false) {
					coInstructors.add(tp.getInstructor());
					this.instructors.add(new TermInstructorView(tp.getInstructor(), IS_ASSIGNED));
				}
			}
		}
	}

	public void setEnrollmentTotal(CourseOffering courseOffering) {
		this.enrollmentTotal = 0;
		for (SectionGroup sectionGroup: courseOffering.getSectionGroups()) {
			for (Section section : sectionGroup.getSections()) {
				for (CensusSnapshot censusSnapshot : section.getCensusSnapshots()) {
					if (censusSnapshot.getSnapshotCode().equals("CURRENT")) {
						this.enrollmentTotal += censusSnapshot.getCurrentEnrollmentCount();
					}
				}
			}
		}
	}
}
