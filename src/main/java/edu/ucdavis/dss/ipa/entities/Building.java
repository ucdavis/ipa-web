package edu.ucdavis.dss.ipa.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;

@Entity
@Table(name = "Buildings")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Building {
	private long id;
	private String name;
	private List<Activity> activities = new ArrayList<Activity>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "BuildingId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(SectionGroupViews.Detailed.class)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "Name", nullable = false, unique = true)
	@JsonProperty("name")
	@JsonView(SectionGroupViews.Detailed.class)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "building")
	public List<Activity> getActivities() {
		return activities;
	}

	public void setActivities(List<Activity> activities) {
		this.activities = activities;
	}
}
