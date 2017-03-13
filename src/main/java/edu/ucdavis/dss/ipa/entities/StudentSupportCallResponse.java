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
    private long id;
    private StudentSupportCall studentSupportCall;
    private SupportStaff supportStaff;
    private Date notifiedAt, warnedAt;
    private boolean submitted;
    private String generalComments, teachingQualifications;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "StudentSupportCallId", nullable = false)
    @NotNull
    @JsonIgnore
    public StudentSupportCall getStudentSupportCall() {
        return studentSupportCall;
    }

    public void setStudentSupportCall(StudentSupportCall studentSupportCall) {
        this.studentSupportCall = studentSupportCall;
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

    @Column (nullable = false)
    public String getTeachingQualifications() {
        return teachingQualifications;
    }

    public void setTeachingQualifications(String teachingQualifications) {
        this.teachingQualifications = teachingQualifications;
    }

    @JsonProperty("studentSupportCallId")
    @Transient
    public long getStudentInstructionalSupportCallIdentification() {
        if(studentSupportCall != null) {
            return studentSupportCall.getId();
        } else {
            return 0;
        }
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
