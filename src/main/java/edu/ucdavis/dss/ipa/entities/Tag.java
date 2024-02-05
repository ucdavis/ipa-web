package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tags")
public class Tag {
	private long id;
	private String name, color;
	private boolean archived;
	private Workgroup workgroup;
	private List<Course> courses = new ArrayList<Course>(0);

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
	@Column(name = "Name", nullable = false, length = 100)
	@JsonProperty
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "WorkgroupId", nullable = false)
	@NotNull
	@JsonIgnore
	public Workgroup getWorkgroup() {
		return this.workgroup;
	}

	public void setWorkgroup(Workgroup workgroup) {
		this.workgroup = workgroup;
	}


	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tags")
	@JsonIgnore
	public List<Course> getCourses()
	{
		return this.courses;
	}

	public void setCourses(List<Course> courses)
	{
		this.courses = courses;
	}

	@Column(name = "archived", unique = false, nullable = false)
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}

	@Basic
	@Column(name = "Color")
	@JsonProperty
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
}
