package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@SuppressWarnings("serial")
@Entity
@Table(name = "WorkgroupCourses")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
  fieldVisibility = JsonAutoDetect.Visibility.NONE,
  getterVisibility = JsonAutoDetect.Visibility.NONE,
  isGetterVisibility = JsonAutoDetect.Visibility.NONE,
  setterVisibility = JsonAutoDetect.Visibility.NONE)
public class WorkgroupCourse extends BaseEntity {
  private long id;
  private Workgroup workgroup;
  private String title, subjectCode, courseNumber, effectiveTermCode;
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "Id", unique = true, nullable = false)
  @JsonProperty
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "WorkgroupId", nullable = false)
  @NotNull
  @JsonIgnore
  public Workgroup getWorkgroup() {
    return this.workgroup;
  }

  public void setWorkgroup(Workgroup workgroup) {
    this.workgroup = workgroup;
  }

  @Basic
  @Column(name = "Title", nullable = false, length = 45)
  @JsonProperty
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @JsonProperty
  public String getSubjectCode() {
    return subjectCode;
  }

  public void setSubjectCode(String subjectCode) {
    this.subjectCode = subjectCode;
  }

  @JsonProperty
  public String getCourseNumber() {
    return courseNumber;
  }

  public void setCourseNumber(String courseNumber) {
    this.courseNumber = courseNumber;
  }

  @NotNull
  @JsonProperty
  public String getEffectiveTermCode() {
    return effectiveTermCode;
  }

  public void setEffectiveTermCode(String effectiveTermCode) {
    this.effectiveTermCode = effectiveTermCode;
  }
}
