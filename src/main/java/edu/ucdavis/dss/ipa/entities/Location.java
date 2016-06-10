package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;

@SuppressWarnings("serial")
@Entity
@Table(name = "Locations")
public class Location implements Serializable {
	private long id;
	private String description;
	private Workgroup workgroup;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "LocationId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "Description", nullable = false, unique = true)
	@JsonProperty("description")
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name= "Workgroups_WorkgroupId", nullable=false)
	@JsonIgnore
	public Workgroup getWorkgroup() {
		return this.workgroup;
	}

	public void setWorkgroup(Workgroup workgroup) {
		this.workgroup = workgroup;
	}

}