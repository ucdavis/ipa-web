package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SupportAssignment is used to record the desire to fill this support 'position'.
 * When an instructionalSupportStaffId is set, that records the assignment into this position.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "SupportAssignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SupportAssignment implements Serializable {
    private long id;
    private SectionGroup sectionGroup;
    private SupportStaff supportStaff;
    private long appointmentPercentage;
    private String appointmentType;

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

    public long getAppointmentPercentage() {
        return appointmentPercentage;
    }

    public void setAppointmentPercentage(long appointmentPercentage) {
        this.appointmentPercentage = appointmentPercentage;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

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

    @Transient
    @JsonIgnore
    public boolean isOpenToReview() {
        // A sectionGroup required for persistence in database, but we should avoid errors if called on a DTO.
        if (this.getSectionGroup() == null) {
            return false;
        }

        String termCode = this.getSectionGroup().getTermCode();

        if (termCode == null || termCode.length() != 6) {
            return false;
        }

        String term = termCode.substring(termCode.length() - 2, termCode.length());
        List<String> openTerms = this.getSectionGroup().getCourse().getSchedule().getInstructorSupportCallReviewAsTerms();

        return (openTerms.indexOf(term) > -1);
    }
}