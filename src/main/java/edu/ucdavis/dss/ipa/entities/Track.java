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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.CourseOfferingGroupViews;

@Entity
@Table(name = "Tracks")
public class Track {
	private long id;
	private String name;
	private boolean archived;
	private Workgroup workgroup;
	private List<CourseOfferingGroup> courseOfferingGroups = new ArrayList<CourseOfferingGroup>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "TrackId", unique = true, nullable = false)
	@JsonProperty
	@JsonView(CourseOfferingGroupViews.Detailed.class)
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
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public String getName()
	{
		return this.name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "Workgroups_WorkgroupId", nullable = false)
	@NotNull
	@JsonIgnore
	public Workgroup getWorkgroup() {
		return this.workgroup;
	}

	public void setWorkgroup(Workgroup workgroup) {
		this.workgroup = workgroup;
	}


	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "tracks")
	@JsonIgnore
	public List<CourseOfferingGroup> getCourseOfferingGroups()
	{
		return this.courseOfferingGroups;
	}

	public void setCourseOfferingGroups(List<CourseOfferingGroup> courseOfferingGroups)
	{
		this.courseOfferingGroups = courseOfferingGroups;
	}

	public void addCourseOfferingGroup(CourseOfferingGroup courseOfferingGroup) {
		addCourseOfferingGroup(courseOfferingGroup, true);
	}

	public void addCourseOfferingGroup(CourseOfferingGroup courseOfferingGroup, boolean add) {
		if (courseOfferingGroup != null) {
			if(getCourseOfferingGroups().contains(courseOfferingGroup)) {
				getCourseOfferingGroups().set(getCourseOfferingGroups().indexOf(courseOfferingGroup), courseOfferingGroup);
			}
			else {
				getCourseOfferingGroups().add(courseOfferingGroup);
			}
			if (add) {
				courseOfferingGroup.addTrack(this, false);
			}
		}
	}

	@Override
	public String toString() {
		return String.format(this.getName());
	}

	@Column(name = "archived", unique = false, nullable = false)
	public boolean isArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}