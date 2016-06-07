package edu.ucdavis.dss.dw.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwSection {
	private long id, maximumEnrollment, actualEnrollment;
	private String crn, sequenceNumber;
	private boolean visible, crnRestricted;
	private List<DwInstructor> instructors;
	private List<DwCensusSnapshot> censusSnapshots;
	private List<DwMeeting> meetings;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getMaximumEnrollment() {
		return maximumEnrollment;
	}
	
	public void setMaximumEnrollment(long maximumEnrollment) {
		this.maximumEnrollment = maximumEnrollment;
	}
	
	public long getActualEnrollment() {
		return actualEnrollment;
	}
	
	public void setActualEnrollment(long actualEnrollment) {
		this.actualEnrollment = actualEnrollment;
	}
	
	public String getCrn() {
		return crn;
	}
	
	public void setCrn(String crn) {
		this.crn = crn;
	}
	
	public String getSequenceNumber() {
		return sequenceNumber;
	}
	
	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isCrnRestricted() {
		return crnRestricted;
	}
	
	public void setCrnRestricted(boolean crnRestricted) {
		this.crnRestricted = crnRestricted;
	}
	
	public List<DwInstructor> getInstructors() {
		return instructors;
	}
	
	public void setInstructors(List<DwInstructor> instructors) {
		this.instructors = instructors;
	}
	
	public List<DwCensusSnapshot> getCensusSnapshots() {
		return censusSnapshots;
	}
	
	public void setCensusSnapshots(List<DwCensusSnapshot> censusSnapshots) {
		this.censusSnapshots = censusSnapshots;
		
		for(DwCensusSnapshot snapshot : this.censusSnapshots) {
			snapshot.setCrn(this.getCrn());
		}
	}

	public List<DwMeeting> getMeetings() {
		return meetings;
	}

	public void setMeetings(List<DwMeeting> meetings) {
		this.meetings = meetings;
	}
}
