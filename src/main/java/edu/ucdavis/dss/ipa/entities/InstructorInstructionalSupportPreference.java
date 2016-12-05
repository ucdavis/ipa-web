package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * InstructorInstructionalSupportPreference records an instructor's preferences for who fills TA positions for courses they are teaching.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructorInstructionalSupportPreferences")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InstructorInstructionalSupportPreference implements Serializable {
    private long id;
    private SectionGroup sectionGroup;
    private InstructionalSupportStaff instructionalSupportStaff;
    private Instructor instructor;
    private String type, comment;
    private long order;
    private InstructorInstructionalSupportCall instructorInstructionalSupportCall;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionGroupId", nullable = false)
    @NotNull
    @JsonIgnore
    public SectionGroup getSectionGroup() {
        return sectionGroup;
    }

    public void setSectionGroup(SectionGroup sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructionalSupportStaffId", nullable = false)
    @JsonIgnore
    public InstructionalSupportStaff getInstructionalSupportStaff() {
        return instructionalSupportStaff;
    }

    public void setInstructionalSupportStaff(InstructionalSupportStaff instructionalSupportStaff) {
        this.instructionalSupportStaff = instructionalSupportStaff;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    @JsonProperty("instructionalSupportStaffId")
    @Transient
    public long getInstructionalSupportStaffIdentification() {
        if(instructionalSupportStaff != null) {
            return instructionalSupportStaff.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("sectionGroupId")
    @Transient
    public long getSectionGroupIdentification() {
        if(sectionGroup != null) {
            return sectionGroup.getId();
        } else {
            return 0;
        }
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