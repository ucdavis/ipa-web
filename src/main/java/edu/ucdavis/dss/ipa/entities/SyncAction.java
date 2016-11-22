package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.api.deserializers.SyncActionDeserializer;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Decisions made by the user to apply to Banner in the DiffView
 *
 * @author Obada Kadri
 */

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = SyncActionDeserializer.class)
@Table(name = "SyncActions")
public class SyncAction {
	private long id;
	private String sectionProperty, childProperty, childUniqueKey;
	private Section section;

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
	@Column(name = "SectionProperty", nullable = true, length = 25)
	@JsonProperty
	public String getSectionProperty() {
		return sectionProperty;
	}

	public void setSectionProperty(String sectionProperty) {
		this.sectionProperty = sectionProperty;
	}

	@Basic
	@Column(name = "ChildProperty", nullable = true, length = 25)
	@JsonProperty
	public String getChildProperty() {
		return childProperty;
	}

	public void setChildProperty(String childProperty) {
		this.childProperty = childProperty;
	}

	@Basic
	@Column(name = "ChildUniqueKey", nullable = true, length = 100)
	@JsonProperty
	public String getChildUniqueKey() {
		return childUniqueKey;
	}

	public void setChildUniqueKey(String childUniqueKey) {
		this.childUniqueKey = childUniqueKey;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SectionId", nullable = false)
	@NotNull
	@JsonIgnore
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	@Transient
	@JsonProperty("sectionId")
	public long getSectionIdentification() {
		if (this.getSection() == null) {
			return 0L;
		}
		return this.getSection().getId();
	}

}