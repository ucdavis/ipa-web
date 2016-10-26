package edu.ucdavis.dss.ipa.api.components.report.views;


import org.javers.core.metamodel.annotation.DiffIgnore;

import javax.persistence.Id;

public class SectionDiffDto {

	@DiffIgnore
	private long id;

	@Id
	private String uniqueKey;

	@DiffIgnore
	private String title;

	private String crn, subjectCode, courseNumber, sequenceNumber;
	private long seats;

	public SectionDiffDto(
			long sectionId,
			String crn,
			String title,
			String subjectCode,
			String courseNumber,
			String sequenceNumber,
			long seats) {
		setUniqueKey(subjectCode + "-" + courseNumber + "-" + sequenceNumber);
		setId(sectionId);
		setCrn(crn);
		setTitle(title);
		setSubjectCode(subjectCode);
		setCourseNumber(courseNumber);
		setSequenceNumber(sequenceNumber);
		setSeats(seats);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getCrn() {
		return crn;
	}

	public void setCrn(String crn) {
		this.crn = crn;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
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
