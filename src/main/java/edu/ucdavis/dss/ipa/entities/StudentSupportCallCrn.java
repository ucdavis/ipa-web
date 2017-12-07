package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "StudentSupportCallCrns")
public class StudentSupportCallCrn extends BaseEntity {
    private long id;
    private StudentSupportCallResponse studentSupportCallResponse;
    private String crn;

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
    @JoinColumn(name = "StudentSupportCallResponseId", nullable = false)
    @NotNull
    @JsonIgnore
    public StudentSupportCallResponse getStudentSupportCallResponse() {
        return studentSupportCallResponse;
    }

    public void setStudentSupportCallResponse(StudentSupportCallResponse studentSupportCallResponse) {
        this.studentSupportCallResponse = studentSupportCallResponse;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }
}
