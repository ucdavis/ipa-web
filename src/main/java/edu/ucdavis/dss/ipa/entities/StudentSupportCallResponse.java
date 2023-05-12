package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
@Entity
@Table(name = "StudentSupportCallResponses")
public class StudentSupportCallResponse implements Serializable {
    private long id, minimumNumberOfPreferences;
    private SupportStaff supportStaff;
    private Date nextContactAt, lastContactedAt, startDate, dueDate;
    private boolean submitted, allowSubmissionAfterDueDate, eligibilityConfirmed;
    private String generalComments, teachingQualifications, message, termCode, availabilityBlob;
    private Schedule schedule;
    private boolean collectGeneralComments, collectTeachingQualifications, collectPreferenceComments;
    private boolean collectEligibilityConfirmation, collectTeachingAssistantPreferences, collectReaderPreferences;
    private boolean collectAssociateInstructorPreferences, requirePreferenceComments, collectAvailabilityByCrn, collectAvailabilityByGrid, collectLanguageProficiencies;
    private boolean sendEmail;
    private Integer languageProficiency;

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

    // The availabilityBlob on a teachingCallResponse is a comma delimited string
    // It represents availability within a 15 hour window (7am-10pm) over 5 days
    // 1 for available, 0 for not
    @Basic(optional = true)
    @JsonProperty
    public String getAvailabilityBlob() {
        return availabilityBlob;
    }

    public void setAvailabilityBlob(String availabilityBlob) {
        this.availabilityBlob = availabilityBlob;
    }

    public boolean isCollectAvailabilityByCrn() {
        return collectAvailabilityByCrn;
    }

    public void setCollectAvailabilityByCrn(boolean collectAvailabilityByCrn) {
        this.collectAvailabilityByCrn = collectAvailabilityByCrn;
    }

    public boolean isCollectAvailabilityByGrid() {
        return collectAvailabilityByGrid;
    }

    public void setCollectAvailabilityByGrid(boolean collectAvailabilityByGrid) {
        this.collectAvailabilityByGrid = collectAvailabilityByGrid;
    }

    public boolean isCollectLanguageProficiencies() {
        return collectLanguageProficiencies;
    }

    public void setCollectLanguageProficiencies(boolean collectLanguageProficiencies) {
        this.collectLanguageProficiencies = collectLanguageProficiencies;
    }

    public Integer getLanguageProficiency() {
        return languageProficiency;
    }

    public void setLanguageProficiency(Integer languageProficiency) {
        this.languageProficiency = languageProficiency;
    }

    @Column (nullable = false)
    public boolean isSendEmail() {
        return sendEmail;
    }

    public void setSendEmail(boolean sendEmail) {
        this.sendEmail = sendEmail;
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

    @Transient
    @JsonProperty
    public static String getDefaultAvailabilityBlob() {
        String blob = "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
                    + "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
                    + "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
                    + "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1," // 30
                    + "1,1,1,1,1,1,1,1,1,1,1,1,1,1,1"; // 29
        return blob;
    }

    /**
     *
     * @param dayIndicator ("M", "T", "W", "R", "F")
     * @return Comma separated string of available times for given day in 12-hour format
     */
    @Transient
    public String describeAvailability(Character dayIndicator) {
        String blob = this.getAvailabilityBlob().replace(",", "");

        Long startHour = 7L;

        Long startTimeBlock = null;
        Long endTimeBlock = null;
        List<String> blocks = new ArrayList<>();

        switch (dayIndicator) {
            case 'M':
                blob = blob.substring(0, 15);
                break;
            case 'T':
                blob = blob.substring(15, 30);
                break;
            case 'W':
                blob = blob.substring(30, 45);
                break;
            case 'R':
                blob = blob.substring(45, 60);
                break;
            case 'F':
                blob = blob.substring(60, 75);
                break;
        }

        int i = 0;
        for (Character hourFlag : blob.toCharArray()) {
            if (hourFlag == '1') {
                if (startTimeBlock == null) {
                    startTimeBlock = startHour + i;
                    endTimeBlock = startHour + i + 1;
                } else {
                    endTimeBlock++;
                }
            } else if (hourFlag == '0' && startTimeBlock != null) {
                blocks.add(blockDescription(startTimeBlock, endTimeBlock));
                startTimeBlock = null;
            }
            i++;
        }

        if (startTimeBlock != null) {
            blocks.add(blockDescription(startTimeBlock, endTimeBlock));
        }

        if (blocks.size() == 0) {
            // No availabilities were indicated
            blocks.add("Not available");
        }

        return String.join(", ", blocks);
    }

    private String blockDescription(Long startTime, Long endTime) {
        String start = (startTime > 12 ? (startTime - 12) + "pm" : startTime + "am");
        String end = (endTime > 12 ? (endTime - 12) + "pm" : endTime + "am");

        return start + "-" + end;
    }
}
