package edu.ucdavis.dss.ipa.api.components.term.views;

import edu.ucdavis.dss.ipa.entities.Instructor;

public class TermInstructorView {
	private String firstName, lastName;
	private long instructorId;
	private boolean isAssigned;

	public TermInstructorView(Instructor instructor) {
		if (instructor == null) return;
		setInstructorId(instructor.getId());
		setFirstName(instructor.getFirstName());
		setLastName(instructor.getLastName());
	}

	public TermInstructorView(Instructor instructor, boolean isAssigned) {
		if (instructor == null) return;
		setInstructorId(instructor.getId());
		setFirstName(instructor.getFirstName());
		setLastName(instructor.getLastName());
		setAssigned(isAssigned);
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public long getInstructorId() {
		return instructorId;
	}

	public void setInstructorId(long instructorId) {
		this.instructorId = instructorId;
	}

	public boolean isAssigned() {
		return isAssigned;
	}

	public void setAssigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
	}

}
