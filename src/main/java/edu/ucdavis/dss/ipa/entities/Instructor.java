package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.entities.validation.Email;
import edu.ucdavis.dss.ipa.web.deserializers.InstructorDeserializer;
import edu.ucdavis.dss.ipa.web.views.InstructorViews;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.web.views.TeachingCallResponseViews;
import edu.ucdavis.dss.ipa.web.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "Instructors")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = InstructorDeserializer.class)
public class Instructor implements Serializable {
	private long id;
	private String firstName;
	private String lastName;
	private String email;
	private String loginId;
	private String employeeId;
	private List<InstructorWorkgroupRelationship> instructorWorkgroupRelationships = new ArrayList<InstructorWorkgroupRelationship>();
	private List<TeachingPreference> teachingPreferences = new ArrayList<TeachingPreference>();
	private List<TeachingCallResponse> teachingCallResponses = new ArrayList<TeachingCallResponse>();
	private List<TeachingCallReceipt> teachingCallReceipts = new ArrayList<TeachingCallReceipt>();
	private List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();
	private List<InstructorTeachingAssistantPreference> instructorTeachingAssistantPreferences = new ArrayList<InstructorTeachingAssistantPreference>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "InstructorId", unique = true, nullable = false)
	@JsonProperty
	@JsonView({
		TeachingCallResponseViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		InstructorViews.Detailed.class,
		TeachingPreferenceViews.Detailed.class
		})
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@Basic
	@Column(name = "FirstName", nullable = false, length = 45)
	@JsonProperty
	@JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
	public String getFirstName()
	{
		return this.firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	@Basic
	@Column(name = "LastName", nullable = false, length = 45)
	@JsonProperty
	@JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
	public String getLastName()
	{
		return this.lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	@JsonProperty
	@Transient
	@JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
	public String getFullName()
	{
		return this.firstName + " " + this.lastName;
	}
	@Basic
	@Column(name = "Email", nullable = true, length = 45, unique = true)
	@JsonProperty("emailAddress")
	@JsonView({TeachingCallResponseViews.Detailed.class, InstructorViews.Detailed.class})
	@Email
	public String getEmail()
	{
		return this.email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	@Basic
	@Column(name = "LoginId", nullable = true, length = 45, unique = true)
	@JsonProperty
	@JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
	public String getLoginId()
	{
		return this.loginId;
	}

	public void setLoginId(String loginId)
	{
		this.loginId = loginId;
	}

	@Basic
	@Column(name = "EmployeeId", nullable = true, length = 9, unique = true)
	@JsonProperty
	@JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
	public String getEmployeeId()
	{
		return this.employeeId;
	}

	public void setEmployeeId(String employeeId)
	{
		this.employeeId = employeeId;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor")
	@JsonIgnore
	public List<TeachingPreference> getTeachingPreferences() {
		return teachingPreferences;
	}

	public void setTeachingPreferences(List<TeachingPreference> teachingPreferences) {
		this.teachingPreferences = teachingPreferences;
	}


	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor")
	@JsonIgnore
	public List<TeachingCallResponse> getTeachingCallResponses() {
		return teachingCallResponses;
	}

	public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
		this.teachingCallResponses = teachingCallResponses;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor")
	@JsonIgnore
	public List<InstructorWorkgroupRelationship> getInstructorWorkgroupRelationships() {
		return instructorWorkgroupRelationships;
	}

	public void setInstructorWorkgroupRelationships(List<InstructorWorkgroupRelationship> instructorWorkgroupRelationships) {
		this.instructorWorkgroupRelationships = instructorWorkgroupRelationships;
	}

	@JsonBackReference
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor")
	public List<InstructorTeachingAssistantPreference> getInstructorTeachingAssistantPreferences() {
		return instructorTeachingAssistantPreferences;
	}

	public void setInstructorTeachingAssistantPreferences(
			List<InstructorTeachingAssistantPreference> instructorTeachingAssistantPreferences) {
		this.instructorTeachingAssistantPreferences = instructorTeachingAssistantPreferences;
	}

	@JsonProperty
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingAssignment> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		this.teachingAssignments = teachingAssignments;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCallReceipt> getTeachingCallReceipts() {
		return teachingCallReceipts;
	}

	public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
		this.teachingCallReceipts = teachingCallReceipts;
	}

}