package edu.ucdavis.dss.ipa.entities;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Holds meta data set by the AC about instructors for a certain schedule
 */
@Entity
@Table(name = "ScheduleInstructorNotes")
public class ScheduleInstructorNote {
	private long id;
	private Instructor instructor;
	private Schedule schedule;
	private Boolean assignmentsCompleted = false;
	private String instructorComment;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "InstructorId", nullable = false)
	@JsonIgnore
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ScheduleId", nullable = false)
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@Column(name = "assignmentsCompleted", nullable = false)
	@JsonProperty
	public Boolean getAssignmentsCompleted() {
		return assignmentsCompleted;
	}

	public void setAssignmentsCompleted(Boolean assignmentsCompleted) {
		this.assignmentsCompleted = assignmentsCompleted;
	}

	@Column(name = "InstructorComment", nullable = true)
	@JsonProperty
	public String getInstructorComment() {
		return instructorComment;
	}

	public void setInstructorComment(String instructorComment) {
		this.instructorComment = instructorComment;
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
}
