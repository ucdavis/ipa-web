package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.api.deserializers.SectionGroupDeserializer;
import edu.ucdavis.dss.ipa.api.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.api.views.ScheduleViews;
import edu.ucdavis.dss.ipa.api.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.api.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "SectionGroups")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = SectionGroupDeserializer.class)
public class SectionGroup implements Serializable {
	private long id;
	private Course course;
	private List<Section> sections;
	private List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
	private String termCode;
	private int PlannedSeats;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonProperty
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<Section> getSections() {
		if (sections == null) sections = new ArrayList<Section>();
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public void addSection(@NotNull @Valid Section section) {
		addSection(section, true);
	}

	public void addSection(@NotNull @Valid Section section, boolean add) {
		if (section != null) {
			if(getSections().contains(section)) {
				getSections().remove(section);
				getSections().add(section);
			} else {
				getSections().add(section);
			}
			if(add) {
				section.setSectionGroup(this);
			}
		}
	}

	@JsonIgnore
	@OneToMany(mappedBy="sectionGroup", cascade=CascadeType.ALL, orphanRemoval = true)
	public List<TeachingAssignment> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		this.teachingAssignments = teachingAssignments;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CourseId", nullable = false)
	@NotNull
	@JsonIgnore
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	@JsonProperty
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@JsonProperty
	public int getPlannedSeats() {
		return PlannedSeats;
	}

	public void setPlannedSeats(int plannedSeats) {
		PlannedSeats = plannedSeats;
	}

	@JsonProperty("courseId")
	@Transient
	public long getCourseIdentification() {
		if(course != null) {
			return course.getId();
		} else {
			return 0;
		}
	}
}
