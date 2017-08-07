package edu.ucdavis.dss.ipa.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SectionGroupCostDeserializer;

@SuppressWarnings("serial")
@Entity
@Table(name = "SectionGroupCostComments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SectionGroupCostComment {
    private long id;
    private User user;
    private SectionGroupCost sectionGroupCost;
    private String comment, authorName;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SectionGroupCostId", nullable = true)
    @NotNull
    @JsonIgnore
    public SectionGroupCost getSectionGroupCost() {
        return sectionGroupCost;
    }

    public void setSectionGroupCost(SectionGroupCost sectionGroupCost) {
        this.sectionGroupCost = sectionGroupCost;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = true)
    @NotNull
    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    @JsonProperty("userId")
    @Transient
    public long getUserId() {
        if(user != null) {
            return user.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("sectionGroupCostId")
    @Transient
    public long getSectionGroupCostId() {
        if(sectionGroupCost != null) {
            return sectionGroupCost.getId();
        } else {
            return 0;
        }
    }
}