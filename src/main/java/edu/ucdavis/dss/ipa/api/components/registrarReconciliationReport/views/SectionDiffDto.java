package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views;


import org.javers.core.metamodel.annotation.DiffIgnore;

import jakarta.persistence.Id;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SectionDiffDto {

	@DiffIgnore
	private long id, sectionGroupId;

	@Id
	private String uniqueKey;

	@DiffIgnore
	private String title;

	private String crn, subjectCode, courseNumber, sequenceNumber;
	private Long seats;
	private Set<InstructorDiffDto> instructors = new HashSet<>();
	private List<ActivityDiffDto> activities = new ArrayList<>();

	public SectionDiffDto() {}

	public SectionDiffDto(
			long sectionId,
			long sectionGroupId,
			String crn,
			String title,
			String subjectCode,
			String courseNumber,
			String sequenceNumber,
			Long seats,
			Set<InstructorDiffDto> instructors,
			List<ActivityDiffDto> activities) {
		setUniqueKey(subjectCode + "-" + courseNumber + "-" + sequenceNumber);
		setId(sectionId);
		setSectionGroupId(sectionGroupId);
		setCrn(crn);
		setTitle(title);
		setSubjectCode(subjectCode);
		setCourseNumber(courseNumber);
		setSequenceNumber(sequenceNumber);
		setSeats(seats);
		setInstructors(instructors);
		setActivities(activities);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSectionGroupId() {
		return sectionGroupId;
	}

	public void setSectionGroupId(long sectionGroupId) {
		this.sectionGroupId = sectionGroupId;
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

	public Long getSeats() {
		return seats;
	}

	public void setSeats(Long seats) {
		this.seats = seats;
	}

	public Set<InstructorDiffDto> getInstructors() {
		return instructors;
	}

	public void setInstructors(Set<InstructorDiffDto> instructors) {
		this.instructors = instructors;
	}

	public List<ActivityDiffDto> getActivities() {
		return activities;
	}

	public void setActivities(List<ActivityDiffDto> activities) {
		this.activities = activities;
	}
}
