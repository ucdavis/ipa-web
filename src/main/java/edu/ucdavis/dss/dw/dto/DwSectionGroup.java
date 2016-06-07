package edu.ucdavis.dss.dw.dto;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwSectionGroup {
	private long id, creditHoursLow, creditHoursHigh;
	private String title, courseNumber, college, courseDescription, gradingMode,
		crossListed, academicLevel, termCode, effectiveTermCode;
	private DwSubject subject;
	private boolean discontinued;
	private DwDepartment department;
	private DwCourse course;
	private List<DwSection> sections = new ArrayList<DwSection>(0);
	
	public long getCreditHoursLow() {
		return creditHoursLow;
	}

	public void setCreditHoursLow(long creditHoursLow) {
		this.creditHoursLow = creditHoursLow;
	}

	public long getCreditHoursHigh() {
		return creditHoursHigh;
	}

	public void setCreditHoursHigh(long creditHoursHigh) {
		this.creditHoursHigh = creditHoursHigh;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public DwCourse getCourse() {
		return course;
	}

	@JsonProperty("course")
	public void setCourse(DwCourse course) {
		this.course = course;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	@JsonProperty("number")
	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getCollege() {
		return college;
	}

	public void setCollege(String college) {
		this.college = college;
	}

	public String getCourseDescription() {
		return courseDescription;
	}

	public void setCourseDescription(String courseDescription) {
		this.courseDescription = courseDescription;
	}

	public String getGradingMode() {
		return gradingMode;
	}

	public void setGradingMode(String gradingMode) {
		this.gradingMode = gradingMode;
	}

	public String getCrossListed() {
		return crossListed;
	}

	public void setCrossListed(String crossListed) {
		this.crossListed = crossListed;
	}

	public String getAcademicLevel() {
		return academicLevel;
	}

	public void setAcademicLevel(String academicLevel) {
		this.academicLevel = academicLevel;
	}

	public boolean isDiscontinued() {
		return discontinued;
	}

	public void setDiscontinued(boolean discontinued) {
		this.discontinued = discontinued;
	}

	public DwSubject getSubject() {
		return subject;
	}

	public void setSubject(DwSubject subject) {
		this.subject = subject;
	}

	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public void setEffectiveTermCode(String effectiveTermCode) {
		this.effectiveTermCode = effectiveTermCode;
	}

	public DwDepartment getDepartment() {
		return department;
	}

	public void setDepartment(DwDepartment department) {
		this.department = department;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Id
	public long getId() {
		return id;
	}
	
	public List<DwSection> getSections() {
		return sections;
	}

	public void setSections(List<DwSection> sections) {
		this.sections = sections;
	}
	
	/**
	 * Returns this section group's sequence pattern which is either
	 * the letter it begins with, e.g. 'A' in the case of A01, A02,
	 * or the full pattern in the case of numeric patterns, e.g. 01.
	 * 
	 * @return
	 */
	public String getSequencePattern() {
		if(this.getSections() == null || this.getSections().size() == 0) return null;
		
		DwSection section = this.getSections().get(0);
		if (section.getSequenceNumber() == null) return null;
		
		char sequenceStartChar = section.getSequenceNumber().charAt(0);
		
		if(Character.isLetter(sequenceStartChar)) {
			return "" + sequenceStartChar;
		} else {
			return section.getSequenceNumber();
		}
	}

	@Override
	public String toString() {
		String subjectCode = "";
		if (this.getSubject() != null) {
			subjectCode = this.getSubject().getCode();
		}

		String courseId = "";
		if (this.getCourse() != null) {
			courseId = String.valueOf(this.getCourse().getId());
		}

		return String.format("[DwSectionGroup id: %d, title: %s, courseNumber: %s, subjectCode: %s, courseId: %s, section count: %d", id, title, courseNumber, subjectCode, courseId, sections.size());
	}
}
