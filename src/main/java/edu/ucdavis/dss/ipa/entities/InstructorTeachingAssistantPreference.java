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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorTeachingAssistantPreferences")
public class InstructorTeachingAssistantPreference implements Serializable {
	private long id, rank;
	private Instructor instructor;
	private SectionGroup sectionGroup;
	private GraduateStudent graduateStudent;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "InstructorTeachingAssistantPreferenceId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonBackReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="SectionGroups_SectionGroupId", nullable=false)
	@JsonProperty("sectionGroup")
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}

	@Basic
	@Column(name = "Rank", nullable = false)
	@JsonProperty("rank")
	public long getRank() {
		return rank;
	}

	public void setRank(long rank) {
		this.rank = rank;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="GraduateStudents_GraduateStudentId", nullable=false)
	@JsonProperty("graduateStudent")
	public GraduateStudent getGraduateStudent() {
		return graduateStudent;
	}

	public void setGraduateStudent(GraduateStudent graduateStudent) {
		this.graduateStudent = graduateStudent;
	}

	@JsonManagedReference
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="Instructors_InstructorId", nullable=false)
	@JsonProperty("instructor")
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}
}