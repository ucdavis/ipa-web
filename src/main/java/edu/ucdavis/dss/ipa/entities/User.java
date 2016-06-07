package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.validation.Email;
import edu.ucdavis.dss.ipa.web.views.UserViews;

@SuppressWarnings("serial")
@Entity
@Table(name = "Users")
public class User implements Serializable {
	private long id;
	private String loginId, email, firstName, lastName, token;
	private Date lastAccessed;
	private List<UserRole> userRoles = new ArrayList<UserRole>(0);

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "UserId", unique = true, nullable = false)
	@JsonProperty
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Basic
	@Column(name = "LoginId", nullable = false, unique = true)
	@JsonProperty("loginId")
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	public String getLoginId() {
		return this.loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	@Basic
	@Column(name = "LastAccessed", nullable = true)
	@JsonProperty
	public Date getLastAccessed() {
		return this.lastAccessed;
	}

	public void setLastAccessed(Date lastAccessed) {
		this.lastAccessed = lastAccessed;
	}

	// Used by Spring Security for generic hasRole calculations
	@Transient
	@JsonIgnore
	public List<String> getRoleAssignments() {
		List<String> roles = new ArrayList<String>();

		for(UserRole userRole : this.getUserRoles()) {
			if( !roles.contains(userRole.getRoleToken()) ) {
				roles.add(userRole.getRoleToken());
			}
		}
		return roles;
	}

	@Transient
	@JsonIgnore
	public boolean isAdmin() {
		for (String role : this.getRoleAssignments()) {
			if (role.equals("admin")) {
				return true;
			}
		}
		return false;
	}

	// Used by Spring Security for generic hasRole calculations
	@JsonIgnore
	@Transient
	public List<Role> getRoles() {
		List<Role> roles = new ArrayList<Role>();

		for(UserRole userRole : this.getUserRoles()) {
			if( !roles.contains(userRole.getRole()) ) {
				roles.add(userRole.getRole());
			}
		}
		return roles;
	}

	/**
	 * Returns a unique list of workgroup codes associated to this user
	 * 
	 * @return
	 */
	@Transient
	public List<Long> getWorkgroupIds() {
		List<Long> workgroupIds = new ArrayList<Long>();

		for(UserRole userRole : this.getUserRoles()) {
			if(userRole.isActive() && userRole.getWorkgroup() != null) {
				if( workgroupIds.contains(userRole.getWorkgroup()) == false ) {
					workgroupIds.add(userRole.getWorkgroup().getId());
				}
			}
		}
		
		return workgroupIds;
	}
	
	/**
	 * Returns a unique list of workgroups associated to this user via userRoles
	 * 
	 * @return
	 */
	@Transient
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@JsonProperty("workgroups")
	public List<Workgroup> getWorkgroups()
	{
		List<Workgroup> workgroups = new ArrayList<Workgroup>();

		for(UserRole userRole : this.getUserRoles()) {
			if(userRole.isActive() && userRole.getWorkgroup() != null) {
				if( !workgroups.contains(userRole.getWorkgroup()) ) {
					workgroups.add(userRole.getWorkgroup());
				}
			}
		}
		return workgroups;
	}

	// Used by Spring Security for hasPermission calculations
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@JsonProperty("roleAssignments")
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "user")
	public List<UserRole> getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(List<UserRole> userRoles) {
		this.userRoles = userRoles;
	}

	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@Basic
	@Column(name = "Email", nullable = false, unique = true)
	@Email
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@Transient
	public String getName() {
		return lastName + ", " + firstName;
	}
	
	@Basic
	@Column(name = "Token", nullable = true, unique = true)
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@Basic
	@Column(name="FirstName")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@JsonView({UserViews.Simple.class,UserViews.Detailed.class})
	@Basic
	@Column(name="LastName")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	@Override
	public String toString() {
		return String.format("User[id=%d,loginId=%s,email=%s,firstName=%s,lastName=%s]", this.getId(), this.getLoginId(), this.getEmail(), this.getFirstName(), this.getLastName());
	}
}
