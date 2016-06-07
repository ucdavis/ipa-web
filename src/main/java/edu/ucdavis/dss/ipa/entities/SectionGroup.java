package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import edu.ucdavis.dss.ipa.web.deserializers.SectionGroupDeserializer;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingGroupViews;
import edu.ucdavis.dss.ipa.web.views.ScheduleViews;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.web.views.TeachingPreferenceViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "SectionGroups")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonDeserialize(using = SectionGroupDeserializer.class)
public class SectionGroup implements Serializable {
	private long id, readerCount = 0, teachingAssistantCount = 0;
	private CourseOffering courseOffering;
	private List<Section> sections;
	private List<TeachingAssistantPreference> teachingAssistantPreferences = new ArrayList<TeachingAssistantPreference>();
	private List<InstructorTeachingAssistantPreference> instructorTeachingAssistantPreferences = new ArrayList<InstructorTeachingAssistantPreference>();
	private List<TeachingAssignment> teachingAssignments = new ArrayList<TeachingAssignment>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "SectionGroupId", unique = true, nullable = false)
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
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

	@Transient
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		TeachingPreferenceViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		ScheduleViews.Detailed.class
		})
	public String getTitle()
	{
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getTitle();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, TeachingPreferenceViews.Detailed.class, SectionGroupViews.Detailed.class})
	public String getSubjectCode()
	{
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getCourse().getSubjectCode();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, TeachingPreferenceViews.Detailed.class, SectionGroupViews.Detailed.class})
	public Long getCourseId()
	{
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getCourse().getId();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, TeachingPreferenceViews.Detailed.class, SectionGroupViews.Detailed.class})
	public String getCourseNumber()
	{
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getCourse().getCourseNumber();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public float getUnitsHigh()
	{
		if (this.getCourseOfferingGroup() == null) return 0;
		return this.getCourseOfferingGroup().getUnitsHigh();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public float getUnitsLow()
	{
		if (this.getCourseOfferingGroup() == null) return 0;
		return this.getCourseOfferingGroup().getUnitsLow();
	}

	@OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "sectionGroup", cascade = {CascadeType.ALL})
	@JsonProperty
	@JsonView({
		CourseOfferingGroupViews.Detailed.class,
		SectionGroupViews.Detailed.class,
		ScheduleViews.Detailed.class
		})
	public List<Section> getSections() {
		if (sections == null) sections = new ArrayList<Section>();
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

	public void addSection(@NotNull @Valid Section section) {
		addSection(section, true);
	}

	public void addSection(@NotNull @Valid Section section, boolean add) {
		if (section != null) {
			if(getSections().contains(section)) {
				getSections().remove(section);
				getSections().add(section);
			} else {
				getSections().add(section);
			}
			if(add) {
				section.setSectionGroup(this);
			}
		}
	}

	@Transient
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public String getTermCode() {
		return this.getCourseOffering().getTermCode();
	}

	@Transient
	public CourseOfferingGroup getCourseOfferingGroup() {
		return this.getCourseOffering().getCourseOfferingGroup();
	}

	@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id")
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sectionGroup")
	public List<TeachingAssistantPreference> getTeachingAssistantPreferences() {
		return teachingAssistantPreferences;
	}

	public void setTeachingAssistantPreferences(List<TeachingAssistantPreference> teachingAssistantPreferences) {
		this.teachingAssistantPreferences = teachingAssistantPreferences;
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public String getEffectiveTermCode() {
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getCourse().getEffectiveTermCode();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public Course getCourse() {
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getCourse();
	}

	@Transient
	@JsonProperty
	@JsonView({CourseOfferingGroupViews.Detailed.class, SectionGroupViews.Detailed.class})
	public List<Track> getTracks() {
		if (this.getCourseOfferingGroup() == null) return null;
		return this.getCourseOfferingGroup().getTracks();
	}

	@Column(name = "ReaderCount", nullable = true)
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public long getReaderCount() {
		return readerCount;
	}

	public void setReaderCount(long readerCount) {
		this.readerCount = readerCount;
	}
	
	@Column(name = "TeachingAssistantCount", nullable = true)
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public long getTeachingAssistantCount() {
		return teachingAssistantCount;
	}

	public void setTeachingAssistantCount(long teachingAssistantCount) {
		this.teachingAssistantCount = teachingAssistantCount;
	}
	
	@JsonManagedReference
	@JsonProperty
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sectionGroup")
	public List<InstructorTeachingAssistantPreference> getInstructorTeachingAssistantPreferences() {
		return instructorTeachingAssistantPreferences;
	}

	public void setInstructorTeachingAssistantPreferences(
			List<InstructorTeachingAssistantPreference> instructorTeachingAssistantPreferences) {
		this.instructorTeachingAssistantPreferences = instructorTeachingAssistantPreferences;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="CourseOfferings_CourseOfferingId", nullable=false)
	public CourseOffering getCourseOffering() {
		return courseOffering;
	}

	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
	}

	@JsonProperty
	@OneToMany(mappedBy="sectionGroup", cascade=CascadeType.ALL, orphanRemoval = true)
	public List<TeachingAssignment> getTeachingAssignments() {
		return teachingAssignments;
	}

	public void setTeachingAssignments(List<TeachingAssignment> teachingAssignments) {
		this.teachingAssignments = teachingAssignments;
	}

	/**
	 * Returns this section group's sequence pattern which is either
	 * the letter it begins with, e.g. 'A' in the case of A01, A02,
	 * or the full pattern in the case of numeric patterns, e.g. 01.
	 * 
	 * @return
	 */
	@Transient
	public String getSequencePattern() {
		if(this.getSections() == null) return null;
		
		Section section = this.getSections().get(0);
		
		char sequenceStartChar = section.getSequenceNumber().charAt(0);
		
		if(Character.isLetter(sequenceStartChar)) {
			return "" + sequenceStartChar;
		} else {
			return section.getSequenceNumber();
		}
	}
}
