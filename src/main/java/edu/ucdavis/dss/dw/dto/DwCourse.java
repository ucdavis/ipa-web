package edu.ucdavis.dss.dw.dto;

import java.util.List;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwCourse {
	private long id, unitsMin, unitsMax, endTermCode;
	private String title, courseNumber, college, courseDescription, gradingMode, crossListed, academicLevel, subjectCode, effectiveTermCode;
	private boolean discontinued;
	private DwSubject subject;
	private DwDepartment department;
	private List<DwSectionGroup> sectionGroups;

	@Id
	public long getId() {
		return id;
	}

	// FIXME: Why is this here? Is this for when the AV imports a new course and uses this DTO?
	public long getCourseId() {
		return getId();
	}

	public long getUnitsMin() {
		return unitsMin;
	}

	public long getUnitsMax() {
		return unitsMax;
	}

	public long getEndTermCode() {
		return endTermCode;
	}

	public String getTitle() {
		return title;
	}

	public String getCourseNumber() {
		return courseNumber;
	}

	public String getCollege() {
		return college;
	}

	public String getCourseDescription() {
		return courseDescription;
	}

	public String getGradingMode() {
		return gradingMode;
	}

	public String getCrossListed() {
		return crossListed;
	}

	public String getAcademicLevel() {
		return academicLevel;
	}

	public String getSubjectCode() {
		return subjectCode;
	}

	public boolean isDiscontinued() {
		return discontinued;
	}

	public DwSubject getSubject() {
		return subject;
	}

	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public DwDepartment getDepartment() {
		return department;
	}

	public List<DwSectionGroup> getSectionGroups() {
		return sectionGroups;
	}

}
