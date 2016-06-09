package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.api.deserializers.TeachingPreferenceDeserializer;
import edu.ucdavis.dss.ipa.api.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingPreferences")
@JsonDeserialize(using = TeachingPreferenceDeserializer.class)
public class TeachingPreference implements Serializable {
	private long id, priority;

	private Instructor instructor;
	private CourseOffering courseOffering;
	private Course course;
	private String notes = "", termCode;	
	private Boolean isBuyout = false, isCourseRelease = false, IsSabbatical = false, approved = false;
	private Schedule schedule;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TeachingPreferenceId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Instructors_InstructorId", nullable = false)
	@NotNull
	@JsonView({TeachingPreferenceViews.Detailed.class})
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CourseOfferings_CourseOfferingId", nullable = true)
	@JsonView({TeachingPreferenceViews.Detailed.class})
	public CourseOffering getCourseOffering() {
		return courseOffering;
	}

	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
	}

	@Basic(optional = false)
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public Boolean getIsBuyout() {
		return isBuyout;
	}

	public void setIsBuyout(Boolean isBuyout) {
		this.isBuyout = isBuyout;
	}

	@Basic(optional = false)
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public Boolean getIsCourseRelease() {
		return isCourseRelease;
	}

	public void setIsCourseRelease(Boolean isCourseRelease) {
		this.isCourseRelease = isCourseRelease;
	}

	@Basic(optional = false)
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public Boolean getIsSabbatical() {
		return IsSabbatical;
	}

	public void setIsSabbatical(Boolean isSabbatical) {
		IsSabbatical = isSabbatical;
	}

	@Basic(optional = false)
	@JsonProperty
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public Boolean isApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	
	@Basic(optional = false)
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	@Basic(optional = false)
	@NotNull
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@Basic(optional = false)
	@NotNull
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public long getPriority() {
		return priority;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	// This is redundant but ensures an association to the schedule remains even when sectionGroup is null
	// In the case of buyout, sabbatical or courseRelease set to true, the sectionGroup will be null and
	// this relationship will be important
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Schedules_ScheduleId", nullable = false)
	@NotNull
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@JsonView(TeachingPreferenceViews.Detailed.class)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Courses_CourseId", nullable = true)
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}
}
