package edu.ucdavis.dss.ipa.security;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.UserRepository;
import edu.ucdavis.dss.ipa.services.AuthenticationService;

public class TokenAuthenticationProvider implements AuthenticationProvider, InitializingBean {
	@Inject UserRepository userRepository;

	private UserDetailsService userDetailsService;

	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		Object credentials = authentication.getCredentials();
		
		if(credentials == null) {
			return null;
		}
		
		String token = credentials.toString();
		List<GrantedAuthority> authorities = new ArrayList<>();

		User user = this.userRepository.findByToken(token);

		if(user != null) {
			/* Update 'lastAccessed' */
			user.setLastAccessed(new java.util.Date());
			this.userRepository.save(user);
			
			AuthenticationService authenticationService = (AuthenticationService)userDetailsService;
			UserDetails userDetails = authenticationService.loadUserByUsername(user.getLoginId());
			
			return new SimpleAuthenticationToken((AuthenticationPrincipal)userDetails, token, authorities);
		} else {
			throw new BadCredentialsException("Unable to authenticate token.");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return (SimpleAuthenticationToken.class.isAssignableFrom(authentication));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.userDetailsService,
				"An authenticationUserDetailsService must be set");
	}
	
	public UserDetailsService getUserDetailsService() {
		return userDetailsService;
	}

	public void setUserDetailsService(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
}
