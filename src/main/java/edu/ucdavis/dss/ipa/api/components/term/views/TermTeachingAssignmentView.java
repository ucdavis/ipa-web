package edu.ucdavis.dss.ipa.api.components.term.views;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;

public class TermTeachingAssignmentView {
	private long id, sectionGroupId;
	private TermInstructorView instructor;

	public TermTeachingAssignmentView(TeachingAssignment teachingAssignment) {
		if (teachingAssignment == null) return;
		setId(teachingAssignment.getId());
		setInstructor(teachingAssignment.getInstructor());
		setSectionGroupId(teachingAssignment.getSectionGroup().getId());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TermInstructorView getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = new TermInstructorView(instructor);
	}

	public long getSectionGroupId() {
		return sectionGroupId;
	}

	public void setSectionGroupId(long sectionGroupId) {
		this.sectionGroupId = sectionGroupId;
	}

}
