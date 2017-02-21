package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingCallReceipts")
public class TeachingCallReceipt implements Serializable {
	private long id;

	private Instructor instructor;
	private Boolean isDone = false, showUnavailabilities = true;
	private Date lastContactedAt, nextContactAt, dueDate;
	private Schedule schedule;
	private String comment, termsBlob, message;

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
	@JoinColumn(name = "Schedules_ScheduleId", nullable = false)
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

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * Terms are expected to be sorted ['01','02','03', '04', '05','06','07','08','09','10']
	 */
	public String getTermsBlob() {
		return termsBlob;
	}

	public void setTermsBlob(String termsBlob) {
		this.termsBlob = termsBlob;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
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

	@JsonProperty("academicYear")
	@Transient
	public long getAcademicYear() {
		if(schedule != null) {
			return schedule.getYear();
		} else {
			return 0;
		}
	}


	@JsonProperty("workgroupId")
	@Transient
	public long getWorkgroupId() {
		if(schedule != null) {
			return schedule.getWorkgroup().getId();
		} else {
			return 0;
		}
	}
}
