package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ActivityLogTag")
public class ActivityLogTag {
    private long id;
    private long activityLogId;
    private String tag;

    @Id
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    @Basic
    @Column(name = "ActivityLogId", unique = true, nullable = false)
    @JsonProperty
    public long getActivityLogId() { return this.activityLogId; }

    public void setActivityLogId(long activityLogId) { this.activityLogId = activityLogId; }

    @Basic
    @Column(name = "Tag", unique = true, nullable = false)
    @JsonProperty
    public String getTag() { return this.tag; }

    public void setTag(String tag) { this.tag = tag; }

}
