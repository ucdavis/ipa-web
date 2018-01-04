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
    private Section section;
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
    @JoinColumn(name = "SectionGroupId", nullable = true)
    @JsonIgnore
    public SectionGroup getSectionGroup() {
        return sectionGroup;
    }

    public void setSectionGroup(SectionGroup sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionId", nullable = true)
    @JsonIgnore
    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
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

    @JsonProperty("sectionId")
    @Transient
    public long getSectionIdentification() {
        if(section != null) {
            return section.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("subjectCode")
    @Transient
    public String getSubjectCode() {
        if(section != null) {
            return section.getSectionGroup().getCourse().getSubjectCode();
        } else if (sectionGroup != null) {
            return sectionGroup.getCourse().getSubjectCode();
        } else {
            return null;
        }
    }

    @JsonProperty("sequenceNumber")
    @Transient
    public String getSequenceNumber() {
        if(section != null) {
            return section.getSequenceNumber();
        } else if (sectionGroup != null) {
            return sectionGroup.getCourse().getSequencePattern();
        } else {
            return null;
        }
    }

    @JsonProperty("courseNumber")
    @Transient
    public String getCourseNumber() {
        if(section != null) {
            return section.getSectionGroup().getCourse().getCourseNumber();
        } else if (sectionGroup != null) {
            return sectionGroup.getCourse().getCourseNumber();
        } else {
            return null;
        }
    }

    /**
     * Will return true if supportAssignment term matches an activated instructorReview term on the schedule
     * Will return null if required entities are not found.
     * @return
     */
    @Transient
    @JsonIgnore
    public Boolean isOpenForInstructorReview() {
        if (this.getSectionGroup() == null) {
            return null;
        }

        String termCode = this.getSectionGroup().getTermCode();

        if (termCode == null || termCode.length() != 6) {
            return null;
        }

        String term = termCode.substring(termCode.length() - 2, termCode.length());
        List<String> openTerms = this.getSectionGroup().getCourse().getSchedule().getInstructorSupportCallReviewAsTerms();

        return (openTerms.indexOf(term) > -1);
    }
}