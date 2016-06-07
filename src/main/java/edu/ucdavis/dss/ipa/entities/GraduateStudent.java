package edu.ucdavis.dss.ipa.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "GraduateStudents")
public class GraduateStudent {
	private long id;
	private Workgroup workgroup;
	private String loginId, firstName, lastName;
	private List<TeachingAssistantPreference> teachingAssistantPreferences = new ArrayList<TeachingAssistantPreference>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "GraduateStudentId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "FirstName", nullable = false)
	@JsonProperty("firstName")
	public String getFirstName() {
		return this.firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	@Basic
	@Column(name = "LastName", nullable = false)
	@JsonProperty("lastName")
	public String getLastName() {
		return this.lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Basic
	@Column(name = "LoginId", nullable = false, unique = true)
	@JsonProperty("loginId")
	public String getLoginId() {
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	@Transient
	@JsonProperty("fullName")
	public String getFullName() {
		return this.lastName + ", " + this.firstName;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "Workgroups_WorkgroupId", nullable=true)
	@JsonProperty("workgroup")
	public Workgroup getWorkgroup() {
		return workgroup;
	}

	public void setWorkgroup(Workgroup workgroup) {
		this.workgroup = workgroup;
	}
	
	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "graduateStudent")
	public List<TeachingAssistantPreference> getTeachingAssistantPreferences() {
		return teachingAssistantPreferences;
	}

	public void setTeachingAssistantPreferences(List<TeachingAssistantPreference> teachingAssistantPreferences) {
		this.teachingAssistantPreferences = teachingAssistantPreferences;
	}
}