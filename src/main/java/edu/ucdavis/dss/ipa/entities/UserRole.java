package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.api.views.UserViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "UserRoles")
public class UserRole implements Serializable {
	private long id;
	private User user;
	private Workgroup workgroup;
	private Role role;
	private Boolean active;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "Id", unique = true, nullable = false)
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "UserId", nullable = false)
	@NotNull
	@JsonIgnore
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "WorkgroupId", nullable = true)
	@JsonIgnore
	public Workgroup getWorkgroup() {
		return workgroup;
	}

	public void setWorkgroup(Workgroup workgroup) {
		this.workgroup = workgroup;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "RoleId", nullable = false)
	@NotNull
	@JsonIgnore
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
	
	@JsonProperty("role")
	@Transient
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	public String getRoleToken() {
		return role.getName();
	}
	
	@JsonProperty("workgroupId")
	@Transient
	@JsonIgnore
	// Renamed to 'getWorkgroupIdentification()' because
	// 'UserRoleRepository.findByWorkgroupId(long id)' got confused
	public long getWorkgroupIdentification() {
		if(workgroup != null) {
			return workgroup.getId();
		} else {
			return 0;
		}
	}

	@Basic
	@Column(name = "Active", nullable = false)
	@JsonProperty("active")
	public Boolean isActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return String.format("UserRole[id=%d,user=%s,role=%s]", this.getId(), this.getUser(), this.getRole());
	}

	/**
	 * Returns a boolean whether the role is an instructor
	 *
	 * @param termCode e.g. for "201510"
	 * @return         2 digit string, e.g. "10"
	 */
	@Transient
	public static boolean isInstructor(UserRole userRole) {
		if (userRole == null || userRole.getRoleToken() == null) { return false; }

		boolean isFederation = userRole.getRoleToken().equals("federationInstructor");
		boolean isSenate = userRole.getRoleToken().equals("senateInstructor");
		return isFederation || isSenate;
	}

	@JsonProperty("userId")
	@Transient
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	// Renamed to 'getUserIdentification()' because
	// 'UserRoleRepository.findByWorkgroupId(long id)' got confused
	public long getUserIdentification() {
		if(user != null) {
			return user.getId();
		} else {
			return 0;
		}
	}
}
