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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.entities.validation.ValidSection;
import edu.ucdavis.dss.ipa.web.deserializers.SectionDeserializer;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.web.views.ScheduleViews;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.web.views.SectionViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "Sections")
@JsonDeserialize(using = SectionDeserializer.class)
@ValidSection
public class Section implements Serializable {
	private long id;
	private long seats;
	private String crn;
	private String sequenceNumber;
	private SectionGroup sectionGroup;
	private List<CensusSnapshot> censusSnapshots = new ArrayList<CensusSnapshot>(0);
	private List<Activity> activities = new ArrayList<Activity>();
	private Boolean visible, crnRestricted;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SectionId", unique = true, nullable = false)
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		SectionViews.Summary.class,
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

	@Basic
	@Column(name = "Seats", nullable = true)
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public long getSeats()
	{
		return this.seats;
	}

	public void setSeats(long seats)
	{
		this.seats = seats;
	}

	@Basic
	@Column(name = "Crn", nullable = true, length = 5)
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public String getCrn()
	{
		return this.crn;
	}

	public void setCrn(String crn)
	{
		this.crn = crn;
	}

	@Basic
	@Column(name = "SequenceNumber", nullable = true, length = 3)
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		SectionViews.Summary.class,
		ScheduleViews.Detailed.class
		})
	public String getSequenceNumber()
	{
		return this.sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionGroups_SectionGroupId", nullable = false)
	@NotNull
	@JsonBackReference
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class, SectionViews.Summary.class})
	public long getSectionGroupId() {
		return this.sectionGroup.getId();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "section", orphanRemoval = true, cascade = {CascadeType.ALL})
	@JsonView({
		ScheduleViews.Detailed.class,
		CourseOfferingGroupViews.Detailed.class,
		SectionViews.Summary.class,
		ScheduleViews.Detailed.class
		})
	public List<CensusSnapshot> getCensusSnapshots() {
		return censusSnapshots;
	}

	public void setCensusSnapshots(List<CensusSnapshot> censusSnapshots) {
		this.censusSnapshots = censusSnapshots;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = {CascadeType.ALL})
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public List<Instructor> getInstructors()
	{
		List<Instructor> instructors = new ArrayList<Instructor>();

		for(TeachingPreference teachingPreference : this.getSectionGroup().getCourseOffering().getTeachingPreferences() ) {
			if(teachingPreference.isApproved().equals(true) ) {
				instructors.add(teachingPreference.getInstructor());
			}
		}
		return instructors;
	}

	@Basic
	@Column(name = "Visible", nullable = true)
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	@Basic
	@Column(name = "CrnRestricted", nullable = true)
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public Boolean isCrnRestricted() {
		return crnRestricted;
	}

	public void setCrnRestricted(Boolean crnRestricted) {
		this.crnRestricted = crnRestricted;
	}

}
