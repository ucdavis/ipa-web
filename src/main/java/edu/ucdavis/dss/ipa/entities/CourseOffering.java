package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.web.deserializers.CourseOfferingDeserializer;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingViews;
import edu.ucdavis.dss.ipa.web.views.ScheduleViews;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.web.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "CourseOfferings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = CourseOfferingDeserializer.class)
public class CourseOffering implements Serializable {
	private long id;
	private Long seatsTotal;
	private CourseOfferingGroup courseOfferingGroup;
	private List<SectionGroup> sectionGroups;
	private List<TeachingPreference> teachingPreferences = new ArrayList<TeachingPreference>();
	private String termCode;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CourseOfferingId", unique = true, nullable = false)
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		CourseOfferingViews.Detailed.class,
		TeachingPreferenceViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		ScheduleViews.Detailed.class
		})
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "courseOffering", cascade = {CascadeType.ALL})
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		ScheduleViews.Detailed.class
		})
	public List<SectionGroup> getSectionGroups() {
		if (sectionGroups == null) sectionGroups = new ArrayList<SectionGroup>();
		return sectionGroups;
	}

	public void setSectionGroups(List<SectionGroup> sectionGroups) {
		this.sectionGroups = sectionGroups;
	}

	public void addSectionGroup(@NotNull @Valid SectionGroup sectionGroup) {
		addSectionGroup(sectionGroup, true);
	}

	public void addSectionGroup(@NotNull @Valid SectionGroup sectionGroup, boolean add) {
		if (sectionGroup != null) {
			if(getSectionGroups().contains(sectionGroup)) {
				getSectionGroups().remove(sectionGroup);
				getSectionGroups().add(sectionGroup);
			} else {
				getSectionGroups().add(sectionGroup);
			}
			if(add) {
				sectionGroup.setCourseOffering(this);
			}
		}
	}

	@Column(name = "TermCode", nullable = false)
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		CourseOfferingViews.Detailed.class,
		SectionGroupViews.Detailed.class})
	public String getTermCode() {
		return termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CourseOfferingGroupId", nullable=false)
	public CourseOfferingGroup getCourseOfferingGroup() {
		return courseOfferingGroup;
	}

	public void setCourseOfferingGroup(CourseOfferingGroup courseOfferingGroup) {
		this.courseOfferingGroup = courseOfferingGroup;
	}

	@Basic
	@Column(name = "SeatsTotal", nullable = true)
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public Long getSeatsTotal() {
		return seatsTotal;
	}

	public void setSeatsTotal(Long seatsTotal) {
		this.seatsTotal = seatsTotal;
	}

	@JsonManagedReference
	@JsonProperty
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "courseOffering", cascade = CascadeType.REMOVE)
	public List<TeachingPreference> getTeachingPreferences() {
		return teachingPreferences;
	}

	public void setTeachingPreferences(List<TeachingPreference> teachingPreferences) {
		this.teachingPreferences = teachingPreferences;
	}

	@Transient
	@JsonView({TeachingPreferenceViews.Detailed.class})
	public String getSubjectCode() {
		return this.getCourseOfferingGroup().getCourse().getSubjectCode();
	}

	@Transient
	@JsonView({TeachingPreferenceViews.Detailed.class})
	public String getCourseNumber() {
		return this.getCourseOfferingGroup().getCourse().getCourseNumber();
	}
}