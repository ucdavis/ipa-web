package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.TeachingAssignmentDeserializer;

/**
 * Represents a teaching assignment or teaching preference (an unapproved assignment that may be created by
 * an instructor in a teaching call). A teaching assignment may indicate a sectionGroup is to be taught
 * by an instructor but may also simply indicate a sabbatical is occurring in a specific term.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingAssignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = TeachingAssignmentDeserializer.class)
public class TeachingAssignment implements Serializable {
	private long id;
	private Instructor instructor;
	private SectionGroup sectionGroup;
	private Schedule schedule;
	private String termCode;
	private int priority;
	private boolean buyout, courseRelease, sabbatical, inResidence, workLifeBalance, leaveOfAbsence;
	private boolean approved;
	private boolean fromInstructor;
	private String suggestedSubjectCode;
	private String suggestedCourseNumber;
	private String suggestedEffectiveTermCode;

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

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "InstructorId", nullable = true)
	@NotNull
	public Instructor getInstructor() {
		return instructor;
	}

	public void setInstructor(Instructor instructor) {
		this.instructor = instructor;
	}

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionGroupId", nullable = false)
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ScheduleId", nullable=false)
	@JsonIgnore
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@JsonProperty
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@JsonProperty
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	@JsonProperty
	public boolean isBuyout() {
		return buyout;
	}

	public void setBuyout(boolean buyout) {
		this.buyout = buyout;
	}

	@JsonProperty
	public boolean isCourseRelease() {
		return courseRelease;
	}

	public void setCourseRelease(boolean courseRelease) {
		this.courseRelease = courseRelease;
	}

	@JsonProperty
	public boolean isSabbatical() {
		return sabbatical;
	}

	public void setSabbatical(boolean sabbatical) {
		this.sabbatical = sabbatical;
	}

	@JsonProperty
	public boolean isInResidence() {
		return inResidence;
	}

	public void setInResidence(boolean inResidence) {
		this.inResidence = inResidence;
	}

	@JsonProperty
	public boolean isApproved() {
		return approved;
	}

	public void setApproved(boolean approved) {
		this.approved = approved;
	}

	@JsonProperty
	public boolean isWorkLifeBalance() {
		return workLifeBalance;
	}

	public void setWorkLifeBalance(boolean workLifeBalance) {
		this.workLifeBalance = workLifeBalance;
	}

	@JsonProperty
	public boolean isLeaveOfAbsence() {
		return leaveOfAbsence;
	}

	public void setLeaveOfAbsence(boolean leaveOfAbsence) {
		this.leaveOfAbsence = leaveOfAbsence;
	}

	@Transient
	@JsonProperty("sectionGroupId")
	public long getSectionGroupIdentification() {
		if (sectionGroup != null) {
			return this.sectionGroup.getId();
		} else {
			return 0;
		}
	}

	@Transient
	@JsonProperty("instructorId")
	public Long getInstructorIdentification() {
		if (this.instructor != null) {
			return this.instructor.getId();
		} else {
			return null;
		}
	}

	@Transient
	@JsonProperty("scheduleId")
	public long getScheduleIdentification() {
		return this.schedule.getId();
	}

	@JsonProperty
	public boolean isFromInstructor() {
		return fromInstructor;
	}

	public void setFromInstructor(boolean fromInstructor) {
		this.fromInstructor = fromInstructor;
	}

	@JsonProperty
	public String getSuggestedSubjectCode() {
		return suggestedSubjectCode;
	}

	public void setSuggestedSubjectCode(String suggestedSubjectCode) {
		this.suggestedSubjectCode = suggestedSubjectCode;
	}

	@JsonProperty
	public String getSuggestedCourseNumber() {
		return suggestedCourseNumber;
	}

	public void setSuggestedCourseNumber(String suggestedCourseNumber) {
		this.suggestedCourseNumber = suggestedCourseNumber;
	}

	@JsonProperty
	public String getSuggestedEffectiveTermCode() {
		return suggestedEffectiveTermCode;
	}

	public void setSuggestedEffectiveTermCode(String suggestedEffectiveTermCode) {
		this.suggestedEffectiveTermCode = suggestedEffectiveTermCode;
	}
}