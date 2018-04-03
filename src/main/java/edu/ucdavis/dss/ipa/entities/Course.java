package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
@Entity
@Table(name = "Courses")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Course extends BaseEntity {
	private long id;
	private String title, subjectCode, courseNumber, effectiveTermCode, sequencePattern;
	private Float unitsLow, unitsHigh;
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
	@JsonDeserialize
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	// TODO: Find a faster way of loading tagId. Lazy loading them slows down the payload
	@Transient
	@JsonProperty
	public List<Long> getTagIds() {
		return this.tags.stream()
				.map(tag -> tag.getId())
				.collect(Collectors.toList());
	}

	@Transient
	@JsonIgnore
	public String getShortDescription() {
		return this.getSubjectCode() + " " + this.getCourseNumber() + " - " + this.getSequencePattern();
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
	public Float getUnitsLow() {
		return unitsLow;
	}

	public void setUnitsLow(Float unitsLow) {
		this.unitsLow = unitsLow;
	}

	@Basic
	@Column(name = "UnitsHigh", nullable = true)
	@JsonProperty
	public Float getUnitsHigh() {
		return unitsHigh;
	}

	public void setUnitsHigh(Float unitsHigh) {
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

	@NotNull
	@JsonProperty
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

	@JsonProperty("scheduleId")
	@Transient
	public long getScheduleIndentification() {
		if(schedule != null) {
			return schedule.getId();
		} else {
			return 0;
		}
	}
}
