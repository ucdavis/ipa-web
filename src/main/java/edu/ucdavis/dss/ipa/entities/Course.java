package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "Courses")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Course implements Serializable {
	private long id;
	private String subjectCode, effectiveTermCode, courseNumber, title;
	private List<CourseOfferingGroup> courseOfferingGroups;
	private List<Course> courseOverlaps = new ArrayList<Course>();

	@JsonView(TeachingPreferenceViews.Detailed.class)
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CourseId", unique = true, nullable = false)
	@JsonProperty
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@JsonView(TeachingPreferenceViews.Detailed.class)
	@Basic
	@Column(name = "SubjectCode", nullable = false, length = 45)
	@JsonProperty
	public String getSubjectCode()
	{
		return this.subjectCode;
	}

	public void setSubjectCode(String subjectCode)
	{
		this.subjectCode = subjectCode;
	}

	@JsonView(TeachingPreferenceViews.Detailed.class)
	@Basic
	@Column(name = "CourseNumber", nullable = false, length = 7)
	@JsonProperty
	public String getCourseNumber()
	{
		return this.courseNumber;
	}

	public void setCourseNumber(String courseNumber)
	{
		this.courseNumber = courseNumber;
	}

	@JsonView(TeachingPreferenceViews.Detailed.class)
	@Basic
	@Column(name = "Title", nullable = false, length = 30)
	@JsonProperty
	public String getTitle()
	{
		return this.title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	@Column(name = "EffectiveTermCode", nullable = false)
	@JsonProperty
	public String getEffectiveTermCode() {
		return effectiveTermCode;
	}

	public void setEffectiveTermCode(String effectiveTermCode) {
		this.effectiveTermCode = effectiveTermCode;
	}
	
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "course", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<CourseOfferingGroup> getCourseOfferingGroups() {
		return courseOfferingGroups;
	}

	public void setCourseOfferingGroups(List<CourseOfferingGroup> courseOfferingGroups) {
		this.courseOfferingGroups = courseOfferingGroups;
	}
	
	@JsonProperty
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "CourseOverlaps", joinColumns = {
			@JoinColumn(name = "CoursesA_CourseId", nullable = false) },
			inverseJoinColumns = { @JoinColumn(name = "CoursesB_CourseId",
			nullable = false) })
	public List<Course> getCourseOverlaps() {
		return courseOverlaps;
	}

	public void setCourseOverlaps(List<Course> courseOverlaps) {
		if(courseOverlaps != null) {
			List<Course> screenedOverlaps = new ArrayList<Course>();
			
			for(Course courseOverlap : courseOverlaps) {
				if(courseOverlap.getId() != this.getId()) {
					screenedOverlaps.add(courseOverlap);
				}
			}
			
			this.courseOverlaps = screenedOverlaps;
		} else {
			this.courseOverlaps = courseOverlaps;
		}
	}

	public void addCourseOverlaps(Course course) {
		if(course.getId() != this.getId()) {
			addCourseOverlaps(course, true);
		}
	}

	public void addCourseOverlaps(Course course, boolean add) {
		if (course != null) {
			if(getCourseOverlaps().contains(course)) {
				getCourseOverlaps().set(getCourseOverlaps().indexOf(course), course);
			}
			else {
				getCourseOverlaps().add(course);
			}
			if (add) {
				course.addCourseOverlaps(this, false);
			}
		}
	}

	public void removeCourseOverlaps(Course course) {
		removeCourseOverlaps(course, true);
	}

	public void removeCourseOverlaps(Course course, boolean remove) {
		if (course != null) {
			if(getCourseOverlaps().contains(course)) {
				getCourseOverlaps().remove(course);
			}
			if (remove) {
				course.removeCourseOverlaps(this, false);
			}
		}
	}

}