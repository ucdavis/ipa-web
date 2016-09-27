package edu.ucdavis.dss.ipa.api.helpers;

import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This filter runs the request through the MultiReadHttpServletRequest which allows multiple reads
 * of the body. This is needed when an exception is thrown and we need to log the request body but
 * it has already been read. In Java EE, a request body can only be read once and without
 * MultiReadHttpServletRequest, Spring will have already read the body in certain cases, like when
 * we use @RequestBody.
 *
 */
public class RequestWrapperFilter implements Filter {
	public void init(FilterConfig config) throws ServletException {
		// Nothing goes here
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws java.io.IOException, ServletException {
		MultiReadHttpServletRequest requestWrapper = new MultiReadHttpServletRequest(request);
		// Pass request back down the filter chain
		chain.doFilter(requestWrapper, response);
	}

	public void destroy() {
		/* Called before the Filter instance is removed from service by the API container */
	}
}