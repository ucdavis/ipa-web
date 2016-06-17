package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.WorkgroupViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "Workgroups")
@JsonAutoDetect(creatorVisibility = JsonAutoDetect.Visibility.NONE,
	fieldVisibility = JsonAutoDetect.Visibility.NONE,
	getterVisibility = JsonAutoDetect.Visibility.NONE,
	isGetterVisibility = JsonAutoDetect.Visibility.NONE,
	setterVisibility = JsonAutoDetect.Visibility.NONE)
public class Workgroup implements Serializable {
	private long id;
	private String name, code;
	private Set<Schedule> schedules = new HashSet<Schedule>();
	private List<Tag> tags = new ArrayList<Tag>();
	private List<UserRole> userRoles = new ArrayList<UserRole>();
	private List<Location> locations = new ArrayList<Location>();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false, length = 250)
	@JsonView(WorkgroupViews.Summary.class)
	public long getId()
	{
		return this.id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	@Basic
	@Column(name = "WorkgroupName", nullable = false, length = 30)
	@NotNull
	@JsonProperty
	@JsonView(WorkgroupViews.Summary.class)
	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	@Basic
	@Column(name = "WorkgroupCode", nullable = false, length = 4, unique = true)
	@JsonProperty
	@JsonView(WorkgroupViews.Summary.class)
	@NotNull
	public String getCode()
	{
		return this.code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}


	@OneToMany(fetch = FetchType.EAGER, mappedBy = "workgroup", orphanRemoval = true, cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workgroup")
	@JsonIgnore
	public Set<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(Set<Schedule> schedules) {
		this.schedules = schedules;
	}

	public void addSchedule(@NotNull @Valid Schedule schedule) {
		addSchedule(schedule, true);
	}

	public void addSchedule(@NotNull @Valid Schedule schedule, boolean add) {
		if (schedule != null) {
			if(getSchedules().contains(schedule)) {
				getSchedules().remove(schedule);
				getSchedules().add(schedule);
			} else {
				getSchedules().add(schedule);
			}
			if(add) {
				schedule.setWorkgroup(this);
			}
		}
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workgroup", orphanRemoval = true, cascade = {CascadeType.ALL})
	@JsonIgnore
	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "workgroup")
	@JsonIgnore
	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

}