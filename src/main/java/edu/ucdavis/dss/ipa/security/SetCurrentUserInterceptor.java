package edu.ucdavis.dss.ipa.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.AuthenticationUser;
import edu.ucdavis.dss.ipa.security.SimpleAuthenticationToken;

public class SetCurrentUserInterceptor implements WebRequestInterceptor {

	@Override
	public void afterCompletion(WebRequest request, Exception ex) throws Exception {
		// No implementation needed.
	}

	@Override
	public void postHandle(WebRequest request, ModelMap model) throws Exception {
		// No implementation needed.
	}

	/**
	 * Called before each HTTP request. Sets the "currentUser" variable in the HTTP request.
	 */
	@Override
	public void preHandle(WebRequest request) throws Exception {
		if(request.getUserPrincipal() == null) return;
		
		Object principal = request.getUserPrincipal();
		AuthenticationPrincipal userDetails = null;
		
		if(principal instanceof CasAuthenticationToken) {
			CasAuthenticationToken userPrincipal = (CasAuthenticationToken)principal;
			
			userDetails = (AuthenticationPrincipal)userPrincipal.getUserDetails();
		} else if(principal instanceof UsernamePasswordAuthenticationToken) {
			// In testing, we use UsernamePasswordAuthenticationToken, not CasAuthenticationToken
			UsernamePasswordAuthenticationToken userPrincipal = (UsernamePasswordAuthenticationToken)principal;
			
			userDetails = (AuthenticationPrincipal)userPrincipal.getPrincipal();
		} else if(principal instanceof SimpleAuthenticationToken) {
			// SimpleAuthenticationToken allows for E2E testing
			SimpleAuthenticationToken userPrincipal = (SimpleAuthenticationToken)principal;
			
			userDetails = (AuthenticationPrincipal)userPrincipal.getPrincipal();
		} else {
			throw new IllegalStateException("Unknown user principal found.");
		}
		
		AuthenticationUser currentUser = userDetails.getUser();
		AuthenticationUser impersonatedUser = userDetails.getImpersonatedUser();
		
		request.setAttribute("currentUser", currentUser, 0);
		request.setAttribute("impersonatedUser", impersonatedUser, 0);
	}
	
}
