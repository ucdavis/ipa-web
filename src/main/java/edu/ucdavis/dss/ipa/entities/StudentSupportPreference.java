package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * StudentSupportPreference records an supportStaff's desire to fill a position of this type and sectionGroup.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "StudentSupportPreferences")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class StudentSupportPreference implements Serializable {
    private long id;
    private SectionGroup sectionGroup;
    private SupportStaff supportStaff;
    private String type, comment, termCode;
    private long priority;
    private Long appointmentPercentage;

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
    @JoinColumn(name = "SupportStaffId", nullable = false)
    @JsonIgnore
    public SupportStaff getSupportStaff() {
        return supportStaff;
    }

    public void setSupportStaff(SupportStaff supportStaff) {
        this.supportStaff = supportStaff;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTermCode() { return termCode; }

    public void setTermCode(String termCode) { this.termCode = termCode; }

    @JsonProperty("supportStaffId")
    @Transient
    public long getInstructionalSupportStaffIdentification() {
        if(supportStaff != null) {
            return supportStaff.getId();
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


    @Column(nullable = true)
    public Long getAppointmentPercentage() {
        return appointmentPercentage;
    }

    public void setAppointmentPercentage(Long appointmentPercentage) {
        this.appointmentPercentage = appointmentPercentage;
    }
}
