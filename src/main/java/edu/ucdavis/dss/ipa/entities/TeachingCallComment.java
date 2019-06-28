package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "TeachingCallComments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TeachingCallComment extends BaseEntity {
    private long id;
    private String comment;
    private TeachingCallReceipt teachingCallReceipt;

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
    @JoinColumn(name = "TeachingCallReceiptId")
    @NotNull
    @JsonIgnore
    public TeachingCallReceipt getTeachingCallReceipt() {
        return teachingCallReceipt;
    }

    public void setTeachingCallReceipt(TeachingCallReceipt teachingCallReceipt) {
        this.teachingCallReceipt = teachingCallReceipt;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @JsonProperty("teachingCallReceiptId")
    @Transient
    public long getTeachingCallReceiptId() {
        if(teachingCallReceipt != null) {
            return teachingCallReceipt.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("lastModifiedOn")
    @Transient
    public Date getLastModifiedOn() {
        return updatedAt;
    }
}
