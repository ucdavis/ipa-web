package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TeachingCallReceipt tracks who has been called for a particular teaching call
 * and related metadata (are they finished, what terms are we asking for, etc.)
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingCallReceipts")
public class TeachingCallReceipt implements Serializable {
	private long id;

	private Instructor instructor;
	private Boolean isDone = false, showUnavailabilities = true, showSeats = true, hideNonCourseOptions = false;
	private boolean sendEmail;
	private Date lastContactedAt, nextContactAt, dueDate;
	private Schedule schedule;
	private String termsBlob, message;
	private List<TeachingCallComment> teachingCallComments = new ArrayList<>();

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

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "teachingCallReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonProperty("comments")
	public List<TeachingCallComment> getTeachingCallComments() { return teachingCallComments; }

	public void setTeachingCallComments(List<TeachingCallComment> teachingCallComments) {
		this.teachingCallComments = teachingCallComments;
	}

	@Column(name = "isDone", nullable = false)
	@JsonProperty
	public Boolean getIsDone() {
		return isDone;
	}

	public void setIsDone(Boolean isDone) {
		this.isDone = isDone;
	}

	public Boolean getShowUnavailabilities() {
		return showUnavailabilities;
	}

	public void setShowUnavailabilities(Boolean showUnavailabilities) {
		this.showUnavailabilities = showUnavailabilities;
	}

	public Boolean getHideNonCourseOptions() {
		return hideNonCourseOptions;
	}

	public void setHideNonCourseOptions(Boolean hideNonCourseOptions) {this.hideNonCourseOptions = hideNonCourseOptions;}

	public Boolean getShowSeats() { return showSeats; }

	public void setShowSeats(Boolean showSeats) { this.showSeats = showSeats; }

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

	@Column (nullable = false)
	public boolean isSendEmail() {
		return sendEmail;
	}

	public void setSendEmail(boolean sendEmail) {
		this.sendEmail = sendEmail;
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

	/**
	 * Returns a list of term codes based on the blob, e.g. a "1" might become 201710
	 * @return
	 */
	@Transient
	public List<String> getTermsBlobAsList() {
		String blob = getTermsBlob();
		List<String> terms = new ArrayList<String>();
		String year = null;

		for(int i = 1; i <= blob.length(); i++) {
			if(blob.charAt(i - 1) == '1') {
				if(i < 5) {
					year = String.valueOf(getAcademicYear() + 1);
				} else {
					year = String.valueOf(getAcademicYear());
				}
				if(i >= 9) {
					terms.add(year + i);
				} else {
					terms.add(year + '0' + i);
				}
			}
		}

        return terms;
    }
}
