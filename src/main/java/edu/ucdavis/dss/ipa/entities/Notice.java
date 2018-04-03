package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "Notices")
public class Notice implements Serializable {
    private long id;
    private String description;
    private Workgroup workgroup;
    private User user;

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

    @Basic
    @Column(name = "Description", nullable = false, unique = true)
    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "WorkgroupId", nullable=false)
    @JsonIgnore
    public Workgroup getWorkgroup() {
        return this.workgroup;
    }

    public void setWorkgroup(Workgroup workgroup) {
        this.workgroup = workgroup;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "UserId", nullable=false)
    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Transient
    @JsonProperty("userId")
    public long getUserIdentification() {
        if (user != null) {
            return user.getId();
        } else {
            return 0;
        }
    }

    @Transient
    @JsonProperty("workgroupId")
    public long getWorkgroupIdentification() {
        if (workgroup != null) {
            return workgroup.getId();
        } else {
            return 0;
        }
    }
}
