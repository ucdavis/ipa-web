package edu.ucdavis.dss.ipa.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.util.Assert;

/**
 * Authentication entry point which allows for a HTTP parameter called
 * 'token' to be used, else it falls back to a provided 'defaultEntryPoint'.
 * 
 * Adapted from Spring's DelegatingAuthenticationEntryPoint.
 * 
 * @author Christopher Thielen
 *
 */
public class TokenAuthenticationEntryPoint implements AuthenticationEntryPoint, InitializingBean {
	private AuthenticationEntryPoint defaultEntryPoint;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {
		String token = request.getParameter("token");
		
		if(token != null) {
			response.sendRedirect("/login/token");
			return;
		}
		
		// 'Token' above did not work. Use the default.
		defaultEntryPoint.commence(request, response, authException);
	}

	/**
	 * EntryPoint which is used when no RequestMatcher returned true
	 */
	public void setDefaultEntryPoint(AuthenticationEntryPoint defaultEntryPoint) {
		this.defaultEntryPoint = defaultEntryPoint;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(defaultEntryPoint, "defaultEntryPoint must be specified");
	}
}
