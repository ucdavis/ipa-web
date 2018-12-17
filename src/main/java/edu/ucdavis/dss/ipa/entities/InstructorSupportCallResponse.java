package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorSupportCallResponses")
public class InstructorSupportCallResponse implements Serializable {
    private long id;
    private Schedule schedule;
    private String message, termCode;
    private Date startDate, dueDate, lastContactedAt, nextContactAt;
    private Instructor instructor;
    private boolean submitted;
    private boolean allowSubmissionAfterDueDate;
    private boolean sendEmail;
    private String generalComments;

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

    @Column (nullable = false)
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @Column (nullable = false)
    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
    }

    @Column (nullable = false)
    public String getGeneralComments() {
        return generalComments;
    }

    public void setGeneralComments(String generalComments) {
        this.generalComments = generalComments;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
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

    public boolean isAllowSubmissionAfterDueDate() {
        return allowSubmissionAfterDueDate;
    }

    public void setAllowSubmissionAfterDueDate(boolean allowSubmissionAfterDueDate) {
        this.allowSubmissionAfterDueDate = allowSubmissionAfterDueDate;
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
