package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ucdavis.dss.ipa.entities.validation.ValidActivity;

import javax.persistence.*;
import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("serial")
@Entity
@Table(name = "ActivityLog")
@ValidActivity
public class ActivityLog {
    private long id;
    private long userId;
    private long timestamp;
    private String message;

    @Id
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    @Basic
    @Column(name = "UsersId")
    @JoinColumn(name= "Id", nullable=false)
    @JsonProperty
    public long getUserId() { return this.userId; }

    public void setUserId(long userId) { this.userId = userId; }

    @Basic
    @Column(name = "Timestamp", unique = false, nullable = false)
    @JsonProperty
    public long getTimestamp() { return this.timestamp; }

    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    @Basic
    @Column(name = "Message", unique = false, nullable = false)
    @JsonProperty
    public String getMessage() { return this.message; }

    public void setMessage(String message) { this.message = message; }
}
