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
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.api.deserializers.TeachingCallResponseDeserializer;
import edu.ucdavis.dss.ipa.api.views.TeachingCallResponseViews;

/**
 * @author okadri
 * Stores instructor availabilities and comments for a given TeachingCall and TermCode
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingCallResponses")
@JsonDeserialize(using = TeachingCallResponseDeserializer.class)
public class TeachingCallResponse implements Serializable {
	private long id;

	private Instructor instructor;
	private String availabilityBlob, termCode;
	private TeachingCall teachingCall;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TeachingCallResponseId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Instructors_InstructorId", nullable = false)
	@NotNull
	@JsonView({TeachingCallResponseViews.Detailed.class})
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	// The availabilityBlob on a teachingCallResponse is a comma delimited string
	// It represents availability within a 15 hour window (7am-10pm) over 5 days
	// 1 for available, 0 for not
	@Basic(optional = false)
	@NotNull
	@Size(min = 149, max = 149)
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public String getAvailabilityBlob() {
		return availabilityBlob;
	}

	public void setAvailabilityBlob(String notes) {
		this.availabilityBlob = notes;
	}

	@Basic(optional = false)
	@NotNull
	@JsonView(TeachingCallResponseViews.Detailed.class)
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TeachingCalls_TeachingCallId", nullable = false)
	@NotNull
	@JsonIgnore
	public TeachingCall getTeachingCall() {
		return teachingCall;
	}

	public void setTeachingCall(TeachingCall teachingCall) {
		this.teachingCall = teachingCall;
	}
}
