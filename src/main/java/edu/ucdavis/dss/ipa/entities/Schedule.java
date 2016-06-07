package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.web.views.ScheduleViews;
import edu.ucdavis.dss.ipa.web.views.WorkgroupViews;

@SuppressWarnings("serial")
@Entity
@Table(
		name = "Schedules",
		uniqueConstraints = {@UniqueConstraint(columnNames={"Workgroups_WorkgroupId", "Year"})}
)
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Schedule implements Serializable {
	private long id;
	private long year;
	private boolean importing;
	private String secretToken;
	private Workgroup workgroup;
	private Set<CourseOfferingGroup> courseOfferingGroups = new HashSet<CourseOfferingGroup>();
	private List<TeachingCall> teachingCalls = new ArrayList<TeachingCall>();
	private List<TeachingPreference> teachingPreferences = new ArrayList<TeachingPreference>();
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ScheduleId", unique = true, nullable = false)
	@JsonProperty("id")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Returns the academic year of the schedule as the first year, e.g. 2016-17 is "2016".
	 * 
	 * @return
	 */
	@Column(name = "Year", unique = false, nullable = false)
	@JsonProperty("year")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public long getYear() {
		return this.year;
	}

	public void setYear(long year) {
		this.year = year;
	}

	@Column(name = "Importing", unique = false, nullable = false)
	@JsonProperty("isImporting")
	@JsonView({WorkgroupViews.Summary.class,ScheduleViews.Summary.class})
	public boolean isImporting() {
		return importing;
	}

	public void setImporting(boolean importing) {
		this.importing = importing;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Workgroups_WorkgroupId", nullable = false)
	@NotNull
	public Workgroup getWorkgroup() {
		return this.workgroup;
	}

	public void setWorkgroup(Workgroup workgroup)
	{
		this.workgroup = workgroup;
		if (!workgroup.getSchedules().contains(this)) {
			workgroup.getSchedules().add(this);
		}
	}

	public String getSecretToken() {
		return secretToken;
	}

	public void setSecretToken(String secretToken) {
		this.secretToken = secretToken;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule")
	@JsonView(ScheduleViews.Detailed.class)
	public Set<CourseOfferingGroup> getCourseOfferingGroups() {
		return courseOfferingGroups;
	}

	public void setCourseOfferingGroups(Set<CourseOfferingGroup> courseOfferingGroups) {
		this.courseOfferingGroups = courseOfferingGroups;
	}

	public void addCourseOfferingGroup(@NotNull @Valid CourseOfferingGroup courseOfferingGroup) {
		addCourseOfferingGroup(courseOfferingGroup, true);
	}

	public void addCourseOfferingGroup(@NotNull @Valid CourseOfferingGroup courseOfferingGroup, boolean add) {
		if (courseOfferingGroup != null) {
			if(getCourseOfferingGroups().contains(courseOfferingGroup)) {
				getCourseOfferingGroups().remove(courseOfferingGroup);
				getCourseOfferingGroups().add(courseOfferingGroup);
			} else {
				getCourseOfferingGroups().add(courseOfferingGroup);
			}
			if(add) {
				courseOfferingGroup.setSchedule(this);
			}
		}
	}

	@JsonView(ScheduleViews.Summary.class)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
	public List<TeachingCall> getTeachingCalls() {
		return teachingCalls;
	}

	public void setTeachingCalls(List<TeachingCall> teachingCalls) {
		this.teachingCalls = teachingCalls;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "schedule")
	public List<TeachingPreference> getTeachingPreferences() {
		return teachingPreferences;
	}

	public void setTeachingPreferences(List<TeachingPreference> teachingPreferences) {
		this.teachingPreferences = teachingPreferences;
	}
}