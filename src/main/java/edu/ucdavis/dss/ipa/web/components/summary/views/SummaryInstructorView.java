package edu.ucdavis.dss.ipa.web.components.summary.views;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.ucdavis.dss.ipa.entities.Instructor;

/**
 * Used by InstructorController to return the summary screen-focused JSON object of an instructor.
 *
 */
public class SummaryInstructorView {
	private String firstName, lastName;
	private long instructorId;
	private List<SummaryScheduleView> summaryScheduleViews = new ArrayList<SummaryScheduleView>();

	@JsonProperty("id")
	public long getInstructorId() {
		return this.instructorId;
	}

	public void setInstructorId(Instructor instructor) {
		this.instructorId = instructor.getId();
	}

	public String getFirstName() {
		return this.firstName;
	}
	
	public void setFirstName(Instructor instructor) {
		this.firstName = instructor.getFirstName();
	}

	public String getLastName() {
		return this.lastName;
	}
	
	public void setLastName(Instructor instructor) {
		this.lastName = instructor.getLastName();
	}

	@JsonProperty("schedules")
	public List<SummaryScheduleView> getSummaryScheduleViews() {
		return this.summaryScheduleViews;
	}
	
	public void setSummaryScheduleViews(List<SummaryScheduleView> views) {
		this.summaryScheduleViews = views;
	}
}
