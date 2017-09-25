package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "StudentSupportCallResponses")
public class StudentSupportCallResponse implements Serializable {
    private long id, minimumNumberOfPreferences;
    private SupportStaff supportStaff;
    private Date nextContactAt, lastContactedAt, startDate, dueDate;
    private boolean submitted, allowSubmissionAfterDueDate, eligibilityConfirmed;
    private String generalComments, teachingQualifications, message, termCode;
    private Schedule schedule;

    private boolean collectGeneralComments, collectTeachingQualifications, collectPreferenceComments;
    private boolean collectEligibilityConfirmation, collectTeachingAssistantPreferences, collectReaderPreferences;
    private boolean collectAssociateInstructorPreferences, requirePreferenceComments;

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
    @JoinColumn(name = "SupportStaffId", nullable = false)
    @NotNull
    @JsonIgnore
    public SupportStaff getSupportStaff() {
        return supportStaff;
    }

    public void setSupportStaff(SupportStaff supportStaff) {
        this.supportStaff = supportStaff;
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

    @Column (nullable = false)
    public String getTeachingQualifications() {
        return teachingQualifications;
    }

    public void setTeachingQualifications(String teachingQualifications) {
        this.teachingQualifications = teachingQualifications;
    }

    public long getMinimumNumberOfPreferences() {
        return minimumNumberOfPreferences;
    }

    public void setMinimumNumberOfPreferences(long minimumNumberOfPreferences) {
        this.minimumNumberOfPreferences = minimumNumberOfPreferences;
    }

    public Date getNextContactAt() {
        return nextContactAt;
    }

    public void setNextContactAt(Date nextContactAt) {
        this.nextContactAt = nextContactAt;
    }

    public Date getLastContactedAt() {
        return lastContactedAt;
    }

    public void setLastContactedAt(Date lastContactedAt) {
        this.lastContactedAt = lastContactedAt;
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

    public boolean isAllowSubmissionAfterDueDate() {
        return allowSubmissionAfterDueDate;
    }

    public void setAllowSubmissionAfterDueDate(boolean allowSubmissionAfterDueDate) {
        this.allowSubmissionAfterDueDate = allowSubmissionAfterDueDate;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ScheduleId", nullable=false)
    @JsonIgnore
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public boolean isCollectGeneralComments() {
        return collectGeneralComments;
    }

    public void setCollectGeneralComments(boolean collectGeneralComments) {
        this.collectGeneralComments = collectGeneralComments;
    }

    public boolean isCollectTeachingQualifications() {
        return collectTeachingQualifications;
    }

    public void setCollectTeachingQualifications(boolean collectTeachingQualifications) {
        this.collectTeachingQualifications = collectTeachingQualifications;
    }

    public boolean isCollectPreferenceComments() {
        return collectPreferenceComments;
    }

    public void setCollectPreferenceComments(boolean collectPreferenceComments) {
        this.collectPreferenceComments = collectPreferenceComments;
    }

    public boolean isCollectEligibilityConfirmation() {
        return collectEligibilityConfirmation;
    }

    public void setCollectEligibilityConfirmation(boolean collectEligibilityConfirmation) {
        this.collectEligibilityConfirmation = collectEligibilityConfirmation;
    }

    public boolean isCollectTeachingAssistantPreferences() {
        return collectTeachingAssistantPreferences;
    }

    public void setCollectTeachingAssistantPreferences(boolean collectTeachingAssistantPreferences) {
        this.collectTeachingAssistantPreferences = collectTeachingAssistantPreferences;
    }

    public boolean isCollectReaderPreferences() {
        return collectReaderPreferences;
    }

    public void setCollectReaderPreferences(boolean collectReaderPreferences) {
        this.collectReaderPreferences = collectReaderPreferences;
    }

    public boolean isCollectAssociateInstructorPreferences() {
        return collectAssociateInstructorPreferences;
    }

    public void setCollectAssociateInstructorPreferences(boolean collectAssociateInstructorPreferences) {
        this.collectAssociateInstructorPreferences = collectAssociateInstructorPreferences;
    }

    public boolean isEligibilityConfirmed() {
        return eligibilityConfirmed;
    }

    public void setEligibilityConfirmed(boolean eligibilityConfirmed) {
        this.eligibilityConfirmed = eligibilityConfirmed;
    }

    public boolean isRequirePreferenceComments() {
        return requirePreferenceComments;
    }

    public void setRequirePreferenceComments(boolean requirePreferenceComments) {
        this.requirePreferenceComments = requirePreferenceComments;
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
}
