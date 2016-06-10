package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.api.deserializers.CourseOfferingGroupDeserializer;
import edu.ucdavis.dss.ipa.api.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.api.views.ScheduleViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "CourseOfferingGroups")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = CourseOfferingGroupDeserializer.class)
public class CourseOfferingGroup implements Serializable {
	private long id;
	private Course course;
	private String title;
	private float unitsLow, unitsHigh;
	private Schedule schedule;
	private List<CourseOffering> courseOfferings = new ArrayList<CourseOffering>();
	private List<Track> tracks = new ArrayList<Track>(0);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "courseOfferingGroup", orphanRemoval = true, cascade = {CascadeType.ALL})
	// @JsonView({CourseOfferingGroupViews.Detailed.class, ScheduleViews.Detailed.class})
	public List<CourseOffering> getCourseOfferings() 
	{
		return this.courseOfferings;
	}

	public void setCourseOfferings(List<CourseOffering> courseOfferings) 
	{
		this.courseOfferings = courseOfferings;
	}

	public void addCourseOffering(CourseOffering courseOffering) 
	{
		addCourseOffering(courseOffering, true);
	}

	public void addCourseOffering(CourseOffering courseOffering, boolean add) 
	{
		if (courseOfferings != null) {
			if(getCourseOfferings().contains(courseOffering)) {
				getCourseOfferings().set(getCourseOfferings().indexOf(courseOffering), courseOffering);
			} else {
				getCourseOfferings().add(courseOffering);
			}
			if(add) {
				courseOffering.setCourseOfferingGroup(this);
			}
		}
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="ScheduleId", nullable=false)
	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	@JsonProperty
	@Transient
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public String getDescription() {
		String desc = course.getSubjectCode() + ' ' + course.getCourseNumber() + ' ' + this.getTitle();
		return desc;
	}

	@JsonProperty
	@Transient
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public HashMap<String, Object> getCourseOfferingInfo() {
		HashMap<String,Object> courseOfferingInfo = new HashMap<String,Object>();
		courseOfferingInfo.put("courseId", course.getId());
		courseOfferingInfo.put("courseNumber", course.getCourseNumber());
		courseOfferingInfo.put("subjectCode", course.getSubjectCode());
		courseOfferingInfo.put("title", this.getTitle());
		courseOfferingInfo.put("effectiveTermCode", course.getEffectiveTermCode());
		courseOfferingInfo.put("unitsLow", this.getUnitsLow());
		courseOfferingInfo.put("unitsHigh", this.getUnitsHigh());

		HashMap<String,Long> sch = new HashMap<String,Long>();
		sch.put("id", this.getSchedule().getId());
		courseOfferingInfo.put("schedule", sch);

		return courseOfferingInfo;
	}

	@Transient
	@JsonView({CourseOfferingGroupViews.Detailed.class, ScheduleViews.Detailed.class})
	public List<SectionGroup> getSectionGroups() {
		List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
		for (CourseOffering courseOffering: this.getCourseOfferings()) {
			sectionGroups.addAll(courseOffering.getSectionGroups());
		}
		return sectionGroups;
	}

	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinTable(name = "CourseOfferingGroups_has_Tracks", joinColumns = { 
			@JoinColumn(name = "CourseOfferingGroups_id", nullable = false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "Tracks_TrackId", 
			nullable = false, updatable = false) })
	@JsonProperty("tracks")
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public List<Track> getTracks() {
		return tracks;
	}

	public void setTracks(List<Track> tracks) {
		this.tracks = tracks;
	}

	public void addTrack(Track track) {
		addTrack(track, true);
	}

	public void addTrack(Track track, boolean add) {
		if (track != null) {
			if(getTracks().contains(track)) {
				getTracks().set(getTracks().indexOf(track), track);
			} else {
				getTracks().add(track);
			}
			if(add) {
				track.addCourseOfferingGroup(this, false);
			}
		}
	}
	
	@JsonProperty("year")
	@Transient
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public long getYear() {
		return schedule.getYear();
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CourseId", nullable=false)
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	@Basic
	@Column(name = "Title", nullable = false, length = 45)
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Basic
	@Column(name = "UnitsLow", nullable = true)
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public float getUnitsLow() {
		return unitsLow;
	}

	public void setUnitsLow(float unitsLow) {
		this.unitsLow = unitsLow;
	}

	@Basic
	@Column(name = "UnitsHigh", nullable = true)
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public float getUnitsHigh() {
		return unitsHigh;
	}

	public void setUnitsHigh(float unitsHigh) {
		this.unitsHigh = unitsHigh;
	}
}
