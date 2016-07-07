package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.CourseOfferingGroupDeserializer;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "Courses")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Course implements Serializable {
	private long id;
	private String title, subjectCode, courseNumber, effectiveTermCode, sequencePattern;
	private float unitsLow, unitsHigh;
	private Schedule schedule;
	private List<SectionGroup> sectionGroups = new ArrayList<>();
	private List<Tag> tags = new ArrayList<Tag>(0);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "course", orphanRemoval = true, cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<SectionGroup> getSectionGroups()
	{
		return this.sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups)
	{
		this.sectionGroups = sectionGroups;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ScheduleId", nullable=false)
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "Courses_has_Tags", joinColumns = {
			@JoinColumn(name = "CourseId", nullable = false, updatable = false) },
			inverseJoinColumns = { @JoinColumn(name = "TagId",
			nullable = false, updatable = false) })
	@JsonIgnore
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	@Transient
	@JsonProperty
	public long getYear() {
		return schedule.getYear();
	}

	@Basic
	@Column(name = "Title", nullable = false, length = 45)
	@JsonProperty
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Basic
	@Column(name = "UnitsLow", nullable = true)
	@JsonProperty
	public float getUnitsLow() {
		return unitsLow;
	}

	public void setUnitsLow(float unitsLow) {
		this.unitsLow = unitsLow;
	}

	@Basic
	@Column(name = "UnitsHigh", nullable = true)
	@JsonProperty
	public float getUnitsHigh() {
		return unitsHigh;
	}

	public void setUnitsHigh(float unitsHigh) {
		this.unitsHigh = unitsHigh;
	}

	@JsonProperty
	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	@JsonProperty
	public String getCourseNumber() {
		return courseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}

	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public void setEffectiveTermCode(String effectiveTermCode) {
		this.effectiveTermCode = effectiveTermCode;
	}

	@JsonProperty
	public String getSequencePattern() {
		return sequencePattern;
	}

	public void setSequencePattern(String sequencePattern) {
		this.sequencePattern = sequencePattern;
	}

}
