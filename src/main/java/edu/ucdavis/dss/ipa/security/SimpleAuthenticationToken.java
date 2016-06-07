package edu.ucdavis.dss.ipa.security;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;

/* Based on source code to UsernamePasswordAuthenticationToken found at:
 * https://github.com/spring-projects/spring-security/blob/master/core/src/main/java/org/springframework/security/authentication/UsernamePasswordAuthenticationToken.java */
@SuppressWarnings("serial")
public class SimpleAuthenticationToken extends AbstractAuthenticationToken {
	private final AuthenticationPrincipal principal;
	private Object credentials;
	
	public SimpleAuthenticationToken(String token) {
		super(null);
		this.principal = null;
		this.credentials = token;
		setAuthenticated(false);
	}

	public SimpleAuthenticationToken(AuthenticationPrincipal principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
		super(authorities);
		
		this.principal = principal;
		this.credentials = credentials;
		
		super.setAuthenticated(true); // must use super, as we override
	}

	public Object getCredentials() {
		return this.credentials;
	}

	public AuthenticationPrincipal getPrincipal() {
		return this.principal;
	}

	public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
		if (isAuthenticated) {
			throw new IllegalArgumentException(
					"Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
		}

		super.setAuthenticated(false);
	}

	@Override
	public void eraseCredentials() {
		super.eraseCredentials();
		credentials = null;
	}
}
