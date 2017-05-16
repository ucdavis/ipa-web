package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SectionDeserializer;
import edu.ucdavis.dss.ipa.entities.validation.ValidSection;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@Entity
@Table(name = "Sections")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonDeserialize(using = SectionDeserializer.class)
public class Section extends BaseEntity {
	private long id;
	private Long seats;
	private String crn;
	private String sequenceNumber;
	private SectionGroup sectionGroup;
	private List<Activity> activities = new ArrayList<>();
	private List<SyncAction> syncActions = new ArrayList<>();
	private Boolean visible, crnRestricted;

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

	@Basic
	@Column(name = "Seats", nullable = true)
	@JsonProperty
	public Long getSeats()
	{
		return this.seats;
	}

	public void setSeats(Long seats)
	{
		this.seats = seats;
	}

	@Basic
	@Column(name = "Crn", nullable = true, length = 5)
	@JsonProperty
	public String getCrn()
	{
		return this.crn;
	}

	public void setCrn(String crn)
	{
		this.crn = crn;
	}

	@Basic
	@Column(name = "SequenceNumber", nullable = false, length = 3)
	@JsonProperty
	public String getSequenceNumber()
	{
		return this.sequenceNumber;
	}

	public void setSequenceNumber(String sequenceNumber)
	{
		this.sequenceNumber = sequenceNumber;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionGroupId", nullable = false)
	@NotNull
	@JsonIgnore
	public SectionGroup getSectionGroup() {
		return sectionGroup;
	}

	public void setSectionGroup(SectionGroup sectionGroup) {
		this.sectionGroup = sectionGroup;
	}

	@Transient
	@JsonProperty
	public long getSectionGroupId() {
		return this.sectionGroup.getId();
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "section", cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<SyncAction> getSyncActions() {
		return syncActions;
	}

	public void setSyncActions(List<SyncAction> syncActions) {
		this.syncActions = syncActions;
	}

	@Basic
	@Column(name = "Visible", nullable = true)
	@JsonProperty
	public Boolean isVisible() {
		return visible;
	}

	public void setVisible(Boolean visible) {
		this.visible = visible;
	}

	@Basic
	@Column(name = "CrnRestricted", nullable = true)
	@JsonProperty
	public Boolean isCrnRestricted() {
		return crnRestricted;
	}

	public void setCrnRestricted(Boolean crnRestricted) {
		this.crnRestricted = crnRestricted;
	}

}
