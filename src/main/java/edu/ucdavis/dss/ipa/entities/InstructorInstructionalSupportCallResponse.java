package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorInstructionalSupportCallResponses")
public class InstructorInstructionalSupportCallResponse implements Serializable {
    private long id;
    private InstructorInstructionalSupportCall instructorInstructionalSupportCall;
    private Instructor instructor;
    private Date notifiedAt, warnedAt;
    private boolean submitted;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorInstructionalSupportCallId", nullable = false)
    @NotNull
    @JsonIgnore
    public InstructorInstructionalSupportCall getInstructorInstructionalSupportCall() {
        return instructorInstructionalSupportCall;
    }

    public void setInstructorInstructionalSupportCall(InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        this.instructorInstructionalSupportCall = instructorInstructionalSupportCall;
    }

    @Column (nullable = false)
    public Date getNotifiedAt() {
        return notifiedAt;
    }

    public void setNotifiedAt(Date notifiedAt) {
        this.notifiedAt = notifiedAt;
    }

    @Column (nullable = false)
    public Date getWarnedAt() {
        return warnedAt;
    }

    public void setWarnedAt(Date warnedAt) {
        this.warnedAt = warnedAt;
    }

    @Column (nullable = false)
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    @Column (nullable = false)
    public String getGeneralComments() {
        return generalComments;
    }

    public void setGeneralComments(String generalComments) {
        this.generalComments = generalComments;
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

    @JsonProperty("instructorSupportCallId")
    @Transient
    public long getInstructorInstructionalSupportCallIdentification() {
        if(instructorInstructionalSupportCall != null) {
            return instructorInstructionalSupportCall.getId();
        } else {
            return 0;
        }
    }
}
