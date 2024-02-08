package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;

//@Entity
public class ActivityLogTag {
    private long id;
    private ActivityLog activityLog;
    private String tag;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    @ManyToOne
    @JoinColumn(name="ActivityLogId", nullable = false)
    @JsonIgnore
    public ActivityLog getActivityLog() { return this.activityLog; }

    public void setActivityLog(ActivityLog activityLog) { this.activityLog = activityLog; }

    @Basic
    @Column(name = "Tag", unique = true, nullable = false)
    @JsonProperty
    public String getTag() { return this.tag; }

    public void setTag(String tag) { this.tag = tag; }

}
