package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * InstructionalSupportAssignment is used to record the desire to fill this support 'position'.
 * When an instructionalSupportStaffId is set, that records the assignment into this position.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructionalSupportAssignment")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InstructionalSupportAssignment implements Serializable {
    private long id;
    private SectionGroup sectionGroup;
    private InstructionalSupportStaff instructionalSupportStaff;
    private Integer appointmentPercentage;
    private String type;

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

    public Integer getAppointmentPercentage() {
        return appointmentPercentage;
    }

    public void setAppointmentPercentage(Integer appointmentPercentage) {
        this.appointmentPercentage = appointmentPercentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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
}