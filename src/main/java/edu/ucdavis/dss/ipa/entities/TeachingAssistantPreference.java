package edu.ucdavis.dss.ipa.entities;

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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "TeachingAssistantPreferences")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class TeachingAssistantPreference {
	private long id, rank;
	private SectionGroup sectionGroup;
	private GraduateStudent graduateStudent;
	private Boolean approved = false;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TeachingAssistantPreferenceId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
	@JsonProperty
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "SectionGroups_SectionGroupId", nullable=false)
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

	@Basic
	@Column(name = "Approved", nullable = false)
	@JsonProperty("approved")
	public Boolean isApproved() {
		return approved;
	}

	public void setApproved(Boolean approved) {
		this.approved = approved;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name= "GraduateStudents_GraduateStudentId", nullable=false)
	@JsonProperty("graduateStudent")
	public GraduateStudent getGraduateStudent() {
		return graduateStudent;
	}

	public void setGraduateStudent(GraduateStudent graduateStudent) {
		this.graduateStudent = graduateStudent;
	}
}