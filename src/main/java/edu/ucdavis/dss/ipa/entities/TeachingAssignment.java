package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.TeachingAssignmentDeserializer;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a teaching assignment or teaching preference (an unapproved assignment that may be created by
 * an instructor in a teaching call). A teaching assignment may indicate a sectionGroup is to be taught
 * by an instructor but may also simply indicate a sabbatical is occurring in a specific term.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "TeachingAssignments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = TeachingAssignmentDeserializer.class)
public class TeachingAssignment implements Serializable {
    private long id;
    private Instructor instructor;
    private SectionGroup sectionGroup;
    private SectionGroupCostInstructor sectionGroupCostInstructor;
    private Schedule schedule;
    private String termCode;
    private int priority;
    private boolean buyout, courseRelease, sabbatical, inResidence, workLifeBalance, leaveOfAbsence, sabbaticalInResidence;
    private boolean approved;
    private boolean fromInstructor;
    private String suggestedSubjectCode;
    private String suggestedCourseNumber;
    private String suggestedEffectiveTermCode;
    private String suggestedTitle;
    private InstructorType instructorType;

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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorId", nullable = true)
    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "InstructorTypeId", nullable = true)
    public InstructorType getInstructorType() {
        return instructorType;
    }

    public void setInstructorType(InstructorType instructorType) {
        this.instructorType = instructorType;
    }

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionGroupId", nullable = false)
    public SectionGroup getSectionGroup() {
        return sectionGroup;
    }

    public void setSectionGroup(SectionGroup sectionGroup) {
        this.sectionGroup = sectionGroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ScheduleId", nullable = false)
    @JsonIgnore
    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @JsonProperty
    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    @JsonProperty
    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @JsonProperty
    public boolean isBuyout() {
        return buyout;
    }

    public void setBuyout(boolean buyout) {
        this.buyout = buyout;
    }

    @JsonProperty
    public boolean isCourseRelease() {
        return courseRelease;
    }

    public void setCourseRelease(boolean courseRelease) {
        this.courseRelease = courseRelease;
    }

    @JsonProperty
    public boolean isSabbatical() {
        return sabbatical;
    }

    public void setSabbatical(boolean sabbatical) {
        this.sabbatical = sabbatical;
    }

    @JsonProperty
    public boolean isInResidence() {
        return inResidence;
    }

    public void setInResidence(boolean inResidence) {
        this.inResidence = inResidence;
    }

    @JsonProperty
    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    @JsonProperty
    public boolean isWorkLifeBalance() {
        return workLifeBalance;
    }

    public void setWorkLifeBalance(boolean workLifeBalance) {
        this.workLifeBalance = workLifeBalance;
    }

    @JsonProperty
    public boolean isLeaveOfAbsence() {
        return leaveOfAbsence;
    }

    public void setLeaveOfAbsence(boolean leaveOfAbsence) {
        this.leaveOfAbsence = leaveOfAbsence;
    }

    public boolean isSabbaticalInResidence() {
        return sabbaticalInResidence;
    }

    public void setSabbaticalInResidence(boolean sabbaticalInResidence) {
        this.sabbaticalInResidence = sabbaticalInResidence;
    }

    @Transient
    @JsonProperty("sectionGroupId")
    public long getSectionGroupIdentification() {
        if (sectionGroup != null) {
            return this.sectionGroup.getId();
        } else {
            return 0;
        }
    }

    @Transient
    @JsonProperty("instructorId")
    public Long getInstructorIdentification() {
        if (this.instructor != null) {
            return this.instructor.getId();
        } else {
            return null;
        }
    }

    @Transient
    @JsonProperty("instructorTypeId")
    public Long getInstructorTypeIdentification() {
        if (this.instructorType != null) {
            return this.instructorType.getId();
        } else {
            return null;
        }
    }

    @Transient
    @JsonProperty("scheduleId")
    public long getScheduleIdentification() {
        return this.schedule.getId();
    }

    @JsonProperty
    public boolean isFromInstructor() {
        return fromInstructor;
    }

    public void setFromInstructor(boolean fromInstructor) {
        this.fromInstructor = fromInstructor;
    }

    @JsonProperty
    public String getSuggestedSubjectCode() {
        return suggestedSubjectCode;
    }

    public void setSuggestedSubjectCode(String suggestedSubjectCode) {
        this.suggestedSubjectCode = suggestedSubjectCode;
    }

    @JsonProperty
    public String getSuggestedCourseNumber() {
        return suggestedCourseNumber;
    }

    public void setSuggestedCourseNumber(String suggestedCourseNumber) {
        this.suggestedCourseNumber = suggestedCourseNumber;
    }

    @JsonProperty
    public String getSuggestedEffectiveTermCode() {
        return suggestedEffectiveTermCode;
    }

    public void setSuggestedEffectiveTermCode(String suggestedEffectiveTermCode) {
        this.suggestedEffectiveTermCode = suggestedEffectiveTermCode;
    }

    @JsonProperty
    public String getSuggestedTitle() {
        return suggestedTitle;
    }

    public void setSuggestedTitle(String suggestedTitle) {
        this.suggestedTitle = suggestedTitle;
    }

    @Transient
    public String getInstructorDisplayName() {
        return this.getInstructor() != null ? this.getInstructor().getLastName() + " " + this.getInstructor().getFirstName().charAt(0) : this.getInstructorType().getDescription();
    }

    @Transient
    public String getDescription() {
        if (this.getSectionGroup() != null && this.getSectionGroup().getCourse() != null) {
            return this.getSectionGroup().getCourse().getShortDescription();
        }
        if (this.isBuyout()) return "Buyout";
        if (this.isCourseRelease()) return "Course Release";
        if (this.isSabbatical()) return "Sabbatical";
        if (this.isInResidence()) return "In Residence";
        if (this.isWorkLifeBalance()) return "Work-life Balance";
        if (this.isLeaveOfAbsence()) return "Leave of Absence";
        if (this.isSabbaticalInResidence()) return "Sabbatical In Residence";

        return null;
    }

    @OneToOne(mappedBy="teachingAssignment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    public SectionGroupCostInstructor getSectionGroupCostInstructor() {
        return sectionGroupCostInstructor;
    }

    public void setSectionGroupCostInstructor(SectionGroupCostInstructor sectionGroupCostInstructor) {
        this.sectionGroupCostInstructor = sectionGroupCostInstructor;
    }
}
