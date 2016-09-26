package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "ActivityLog")
public class ActivityLog {
    private long id;
    private User user;
    private Timestamp timestamp;
    private String message;

    @Id
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    @ManyToOne
    @JoinColumn(name= "UsersId", nullable=false)
    @JsonProperty
    public User getUser() { return this.user; }

    public void setUser(User user) { this.user = user; }

    @Basic
    @Column(name = "Timestamp", unique = false, nullable = false)
    @JsonProperty
    public Timestamp getTimestamp() { return this.timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Basic
    @Column(name = "Message", unique = false, nullable = false)
    @JsonProperty
    public String getMessage() { return this.message; }

    public void setMessage(String message) { this.message = message; }
}
