package edu.ucdavis.dss.ipa.api.components.course.views;

public class SectionGroupImport {
	private String subjectCode, courseNumber, sequencePattern, termCode, title, effectiveTermCode;
	private int plannedSeats;
	private Long unitsLow, unitsHigh;

	public String getSubjectCode()
	{
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

	public String getSequencePattern() {
		return sequencePattern;
	}

	public void setSequencePattern(String sequencePattern) {
		this.sequencePattern = sequencePattern;
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public int getPlannedSeats() {
		return plannedSeats;
	}

	public void setPlannedSeats(int plannedSeats) {
		this.plannedSeats = plannedSeats;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public void setEffectiveTermCode(String effectiveTermCode) {
		this.effectiveTermCode = effectiveTermCode;
	}

	public Long getUnitsLow() {
		return unitsLow;
	}

	public void setUnitsLow(Long unitsLow) {
		this.unitsLow = unitsLow;
	}

	public Long getUnitsHigh() {
		return unitsHigh;
	}

	public void setUnitsHigh(Long unitsHigh) {
		this.unitsHigh = unitsHigh;
	}
}
