package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.web.deserializers.TeachingPreferenceDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingAssignments")
@JsonDeserialize(using = TeachingPreferenceDeserializer.class)
public class TeachingAssignment implements Serializable {
	private long id;
	private Instructor instructor;
	private SectionGroup sectionGroup;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TeachingAssignmentId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Instructors_InstructorId", nullable = false)
	@NotNull
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionGroups_SectionGroupId", nullable = false)
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}
}
