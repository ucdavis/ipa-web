package edu.ucdavis.dss.ipa.api.components.assignment.views;

import edu.ucdavis.dss.ipa.entities.Instructor;

/**
 * Used by CourseOfferingGroupController to return the list of cogs and their instructors
 * for a given schedule
 * Path: /api/schedules/{scheduleId}/teachingCallByCourse
 *
 * @author okadri
 * 
 */
public class TeachingCallInstructorView {
	private String firstName, lastName;
	private long id;

	public TeachingCallInstructorView(Instructor instructor) {
		setId(instructor.getId());
		setFirstName(instructor.getFirstName());
		setLastName(instructor.getLastName());
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

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

}
