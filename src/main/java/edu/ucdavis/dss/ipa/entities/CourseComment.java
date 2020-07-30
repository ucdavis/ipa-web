package edu.ucdavis.dss.ipa.entities;

import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@Entity
@Table(name = "CourseComments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourseComment extends BaseEntity {
    private long id;
    private User user;
    private Course course;
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
    @JoinColumn(name = "CourseId", nullable = false)
    @NotNull
    @JsonIgnore
    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", nullable = false)
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

    @JsonProperty("courseId")
    @Transient
    public long getCourseId() {
        if(course != null) {
            return course.getId();
        } else {
            return 0;
        }
    }

    @JsonProperty("creationDate")
    @Transient
    public Date getCreationDate() {
        return createdAt;
    }
}
