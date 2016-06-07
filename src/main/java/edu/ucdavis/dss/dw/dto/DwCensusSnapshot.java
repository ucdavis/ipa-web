package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwCensusSnapshot {
	private long id, currentAvailableSeatCount, currentEnrollmentCount, maxEnrollmentCount,
	studentCount, waitCount, waitCapacityCount;
	private String snapshotCode;
	private String crn;
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getCurrentAvailableSeatCount() {
		return currentAvailableSeatCount;
	}
	
	public void setCurrentAvailableSeatCount(long currentAvailableSeatCount) {
		this.currentAvailableSeatCount = currentAvailableSeatCount;
	}
	
	public long getCurrentEnrollmentCount() {
		return currentEnrollmentCount;
	}
	
	public void setCurrentEnrollmentCount(long currentEnrollmentCount) {
		this.currentEnrollmentCount = currentEnrollmentCount;
	}
	
	public long getMaxEnrollmentCount() {
		return maxEnrollmentCount;
	}
	
	public void setMaxEnrollmentCount(long maxEnrollmentCount) {
		this.maxEnrollmentCount = maxEnrollmentCount;
	}
	
	public long getStudentCount() {
		return studentCount;
	}
	
	public void setStudentCount(long studentCount) {
		this.studentCount = studentCount;
	}
	
	public long getWaitCount() {
		return waitCount;
	}
	
	public void setWaitCount(long waitCount) {
		this.waitCount = waitCount;
	}
	
	public long getWaitCapacityCount() {
		return waitCapacityCount;
	}
	
	public void setWaitCapacityCount(long waitCapacityCount) {
		this.waitCapacityCount = waitCapacityCount;
	}
	
	public String getSnapshotCode() {
		return snapshotCode;
	}
	
	public void setSnapshotCode(String snapshotCode) {
		this.snapshotCode = snapshotCode;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}
}
