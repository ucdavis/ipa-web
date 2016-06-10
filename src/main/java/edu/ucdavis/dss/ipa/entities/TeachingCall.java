package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
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
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallViews;
import edu.ucdavis.dss.ipa.api.deserializers.TeachingCallDeserializer;
import edu.ucdavis.dss.ipa.api.views.ScheduleViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingCalls")
@JsonDeserialize(using = TeachingCallDeserializer.class)
public class TeachingCall implements Serializable {
	private long id;

	private Schedule schedule;
	private String message;
	private java.sql.Date startDate, dueDate;
	private boolean sentToSenate, sentToFederation, emailInstructors, showUnavailabilities;
	private List<TeachingCallResponse> teachingCallResponses;
	private List<TeachingCallReceipt> teachingCallReceipts;
	private String termsBlob;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TeachingCallId", unique = true, nullable = false)
	@JsonProperty
	@JsonView({ScheduleViews.Summary.class, TeachingCallViews.Detailed.class})
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

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

	@Column(name = "Message", nullable = true)
	@JsonProperty
	@JsonView(TeachingCallViews.Detailed.class)
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Column(name = "DueDate", nullable = false)
	@JsonProperty
	@JsonView(TeachingCallViews.Detailed.class)
	public java.sql.Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(java.sql.Date dueDate) {
		this.dueDate = dueDate;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teachingCall", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCallResponse> getTeachingCallResponses() {
		return teachingCallResponses;
	}

	public void setTeachingCallResponses(List<TeachingCallResponse> teachingCallResponses) {
		this.teachingCallResponses = teachingCallResponses;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teachingCall", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCallReceipt> getTeachingCallReceipts() {
		return teachingCallReceipts;
	}

	public void setTeachingCallReceipts(List<TeachingCallReceipt> teachingCallReceipts) {
		this.teachingCallReceipts = teachingCallReceipts;
	}

	@Column(name = "StartDate", nullable = false)
	@JsonProperty
	@JsonView(TeachingCallViews.Detailed.class)
	public java.sql.Date getStartDate() {
		return startDate;
	}

	public void setStartDate(java.sql.Date startDate) {
		this.startDate = startDate;
	}

	@Column(name = "SentToSenate", nullable = false)
	@JsonProperty
	@JsonView({ScheduleViews.Summary.class, TeachingCallViews.Detailed.class})
	public boolean isSentToSenate() {
		return sentToSenate;
	}

	public void setSentToSenate(boolean sentToSenate) {
		this.sentToSenate = sentToSenate;
	}

	@Column(name = "SentToFederation", nullable = false)
	@JsonProperty
	@JsonView({ScheduleViews.Summary.class, TeachingCallViews.Detailed.class})
	public boolean isSentToFederation() {
		return sentToFederation;
	}

	public void setSentToFederation(boolean sentToFederation) {
		this.sentToFederation = sentToFederation;
	}

	@Transient
	@JsonIgnore
	public static String DefaultBlob() {
		String blob = "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
				+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
				+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
				+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
				+ "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1"; // 29
		return blob;
	}

	@Column(name = "emailInstructors", nullable = false)
	public boolean isEmailInstructors() {
		return emailInstructors;
	}

	public void setEmailInstructors(boolean emailInstructors) {
		this.emailInstructors = emailInstructors;
	}

	@Column(name = "ShowUnavailabilities", nullable = false)
	@JsonProperty
	@JsonView({ScheduleViews.Summary.class, TeachingCallViews.Detailed.class})
	public boolean isShowUnavailabilities() {
		return showUnavailabilities;
	}

	public void setShowUnavailabilities(boolean showUnavailabilities) {
		this.showUnavailabilities = showUnavailabilities;
	}

	// Terms are expected to be sorted ['05','06','07','08','09','10','01','02','03']
	@JsonProperty
	@Column(name = "TermsBlob", nullable = false)
	public String getTermsBlob() {
		return termsBlob;
	}

	public void setTermsBlob(String termsBlob) {
		this.termsBlob = termsBlob;
	}
}
