package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SectionGroupDeserializer;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
	private List<SupportAssignment> supportAssignments;
	private List<StudentSupportPreference> studentInstructionalSupportCallPreferences;
	private List<InstructorSupportPreference> instructorSupportPreferences;
	private List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
	private List<Activity> activities = new ArrayList<Activity>();
	private String termCode;
	private Integer PlannedSeats;

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

	@Column(name = "PlannedSeats", nullable = true)
	@JsonProperty
	public Integer getPlannedSeats() {
		return PlannedSeats;
	}

	public void setPlannedSeats(Integer plannedSeats) {
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

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}


	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<SupportAssignment> getSupportAssignments() {
		return supportAssignments;
	}

	public void setSupportAssignments(List<SupportAssignment> supportAssignments) {
		this.supportAssignments = supportAssignments;
	}

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<StudentSupportPreference> getStudentInstructionalSupportCallPreferences() {
		return studentInstructionalSupportCallPreferences;
	}

	public void setStudentInstructionalSupportCallPreferences(List<StudentSupportPreference> studentInstructionalSupportCallPreferences) {
		this.studentInstructionalSupportCallPreferences = studentInstructionalSupportCallPreferences;
	}

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<InstructorSupportPreference> getInstructorSupportPreferences() {
		return instructorSupportPreferences;
	}

	public void setInstructorSupportPreferences(List<InstructorSupportPreference> instructorSupportPreferences) {
		this.instructorSupportPreferences = instructorSupportPreferences;
	}
}
