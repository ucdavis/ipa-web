package edu.ucdavis.dss.ipa.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AuthenticationPrincipal implements UserDetails {
	private AuthenticationUser user, impersonatedUser;
	private long activeWorkgroupId;
	private String activeWorkgroupName;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		AuthenticationUser currentUser = getCurrentUser();
		List<GrantedAuthority> authList = getGrantedAuthorities(currentUser.getRoles());

		return authList;
	}

	private AuthenticationUser getCurrentUser() {
		if (impersonatedUser != null) {
			return impersonatedUser;
		} else {
			return user;
		}
	}

	public static List<GrantedAuthority> getGrantedAuthorities(List<String> roles) {
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		for (String role : roles) {
			authorities.add(new SimpleGrantedAuthority(role));
		}

		return authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return getCurrentUser().getLoginid();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return true;
	}

	public AuthenticationUser getUser() {
		return user;
	}

	public void setUser(AuthenticationUser user) {
		this.user = user;
	}

	public AuthenticationUser getImpersonatedUser() {
		return impersonatedUser;
	}

	public void setImpersonatedUser(AuthenticationUser impersonatedUser) {
		this.impersonatedUser = impersonatedUser;
	}

	public long getActiveWorkgroupId() {
		return activeWorkgroupId;
	}

	public void setActiveWorkgroupId(long activeWorkgroupId) {
		this.activeWorkgroupId = activeWorkgroupId;
	}

	public String getActiveWorkgroupName() {
		return activeWorkgroupName;
	}
	
	public void setActiveWorkgroupName(String activeWorkgroupName) {
		this.activeWorkgroupName = activeWorkgroupName;
	}
	
	@JsonIgnore
	public String getToJson() throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writeValueAsString(this);
	}

}
