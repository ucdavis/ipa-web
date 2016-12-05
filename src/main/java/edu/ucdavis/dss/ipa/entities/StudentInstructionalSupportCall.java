package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.InstructorInstructionalSupportCallDeserializer;
import edu.ucdavis.dss.ipa.api.deserializers.StudentInstructionalSupportCallDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "StudentInstructionalSupportCalls")
@JsonDeserialize(using = StudentInstructionalSupportCallDeserializer.class)
public class StudentInstructionalSupportCall implements Serializable {
    private long id;
    private long MinimumNumberOfPreferences;
    private Schedule schedule;
    private String message, termCode;
    private Date startDate, dueDate;
    private boolean sendEmails, AllowSubmissionAfterDueDate;
    private boolean CollectGeneralComments, CollectTeachingQualifications, CollectPreferenceComments;
    private boolean CollectEligibilityConfirmation, CollectTeachingAssistantPreferences, CollectReaderPreferences;
    private boolean CollectAssociateInstructorPreferences;
    private List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses = new ArrayList<>();
    private List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences = new ArrayList<>();

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
    @JoinColumn(name = "ScheduleId", nullable = false)
    @NotNull
    @JsonIgnore
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @Column(name = "Message", nullable = true)
    @JsonProperty
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Column(name = "DueDate", nullable = false)
    @JsonProperty
    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Column(name = "StartDate", nullable = false)
    @JsonProperty
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @JsonProperty
    @Column(nullable = false)
    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isSendEmails() {
        return sendEmails;
    }

    public void setSendEmails(boolean sendEmails) {
        this.sendEmails = sendEmails;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isAllowSubmissionAfterDueDate() {
        return AllowSubmissionAfterDueDate;
    }

    public void setAllowSubmissionAfterDueDate(boolean allowSubmissionAfterDueDate) {
        AllowSubmissionAfterDueDate = allowSubmissionAfterDueDate;
    }

    @Column(nullable = false)
    public long getMinimumNumberOfPreferences() {
        return MinimumNumberOfPreferences;
    }

    public void setMinimumNumberOfPreferences(long minimumNumberOfPreferences) {
        MinimumNumberOfPreferences = minimumNumberOfPreferences;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectGeneralComments() {
        return CollectGeneralComments;
    }

    public void setCollectGeneralComments(boolean collectGeneralComments) {
        CollectGeneralComments = collectGeneralComments;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectTeachingQualifications() {
        return CollectTeachingQualifications;
    }

    public void setCollectTeachingQualifications(boolean collectTeachingQualifications) {
        CollectTeachingQualifications = collectTeachingQualifications;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectPreferenceComments() {
        return CollectPreferenceComments;
    }

    public void setCollectPreferenceComments(boolean collectPreferenceComments) {
        CollectPreferenceComments = collectPreferenceComments;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectEligibilityConfirmation() {
        return CollectEligibilityConfirmation;
    }

    public void setCollectEligibilityConfirmation(boolean collectEligibilityConfirmation) {
        CollectEligibilityConfirmation = collectEligibilityConfirmation;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectTeachingAssistantPreferences() {
        return CollectTeachingAssistantPreferences;
    }

    public void setCollectTeachingAssistantPreferences(boolean collectTeachingAssistantPreferences) {
        CollectTeachingAssistantPreferences = collectTeachingAssistantPreferences;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectReaderPreferences() {
        return CollectReaderPreferences;
    }

    public void setCollectReaderPreferences(boolean collectReaderPreferences) {
        CollectReaderPreferences = collectReaderPreferences;
    }

    @JsonProperty
    @Column(nullable = false)
    public boolean isCollectAssociateInstructorPreferences() {
        return CollectAssociateInstructorPreferences;
    }

    public void setCollectAssociateInstructorPreferences(boolean collectAssociateInstructorPreferences) {
        CollectAssociateInstructorPreferences = collectAssociateInstructorPreferences;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "studentInstructionalSupportCall", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    public List<StudentInstructionalSupportCallResponse> getStudentInstructionalSupportCallResponses() {
        return studentInstructionalSupportCallResponses;
    }

    public void setStudentInstructionalSupportCallResponses(List<StudentInstructionalSupportCallResponse> studentInstructionalSupportCallResponses) {
        this.studentInstructionalSupportCallResponses = studentInstructionalSupportCallResponses;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "studentInstructionalSupportCall", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    public List<StudentInstructionalSupportPreference> getStudentInstructionalSupportPreferences() {
        return studentInstructionalSupportPreferences;
    }

    public void setStudentInstructionalSupportPreferences(List<StudentInstructionalSupportPreference> studentInstructionalSupportPreferences) {
        this.studentInstructionalSupportPreferences = studentInstructionalSupportPreferences;
    }

    @JsonProperty("scheduleId")
    @Transient
    public long getScheduleIdentification() {
        if(schedule != null) {
            return schedule.getId();
        } else {
            return 0;
        }
    }
}
