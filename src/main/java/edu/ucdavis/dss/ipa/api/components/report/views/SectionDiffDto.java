package edu.ucdavis.dss.ipa.api.components.report.views;

public class SectionDiffDto {
	private String crn, subjectCode, courseNumber, sequenceNumber;
	private long seats;

	public SectionDiffDto(
			String crn,
			String subjectCode,
			String courseNumber,
			String sequenceNumber,
			long seats) {
		setCrn(crn);
		setSubjectCode(subjectCode);
		setCourseNumber(courseNumber);
		setSequenceNumber(sequenceNumber);
		setSeats(seats);
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
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

	public String getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public long getSeats() {
		return seats;
	}

	public void setSeats(long seats) {
		this.seats = seats;
	}
}
