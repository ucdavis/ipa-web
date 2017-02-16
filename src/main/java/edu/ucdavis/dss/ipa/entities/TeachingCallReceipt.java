package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.TeachingCallReceiptViews;

/**
 * @author okadri
 * Stores instructor's notification time-stamps for a given TeachingCall 
 * It also has a flag to indicate whether the instructor is done inputing
 * preferences for the year
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingCallReceipts")
public class TeachingCallReceipt implements Serializable {
	private long id;

	private Instructor instructor;
	private Boolean isDone = false, showUnavailabilities = true;
	private Date lastContactedAt, nextContactAt;
	private Schedule schedule;
	private String comment, termsBlob;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "InstructorId", nullable = false)
	@NotNull
	@JsonIgnore
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TeachingCallId", nullable = false)
	@NotNull
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Column(name = "isDone", nullable = false)
	@JsonProperty
	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}

	@Column(name = "Comment", nullable = true)
	@JsonProperty
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public Boolean getShowUnavailabilities() {
		return showUnavailabilities;
	}

	public void setShowUnavailabilities(Boolean showUnavailabilities) {
		this.showUnavailabilities = showUnavailabilities;
	}

	public Date getLastContactedAt() {
		return lastContactedAt;
	}

	public void setLastContactedAt(Date lastContactedAt) {
		this.lastContactedAt = lastContactedAt;
	}

	public Date getNextContactAt() {
		return nextContactAt;
	}

	public void setNextContactAt(Date nextContactAt) {
		this.nextContactAt = nextContactAt;
	}

	public String getTermsBlob() {
		return termsBlob;
	}

	public void setTermsBlob(String termsBlob) {
		this.termsBlob = termsBlob;
	}

	@JsonProperty("instructorId")
	@Transient
	public long getInstructorIdentification() {
		if(instructor != null) {
			return instructor.getId();
		} else {
			return 0;
		}
	}

	@JsonProperty("scheduleId")
	@Transient
	public long getScheduleIndentification() {
		if(schedule != null) {
			return schedule.getId();
		} else {
			return 0;
		}
	}

}
