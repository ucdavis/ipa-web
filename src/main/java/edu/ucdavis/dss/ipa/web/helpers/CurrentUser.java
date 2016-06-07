package edu.ucdavis.dss.ipa.web.helpers;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import edu.ucdavis.dss.ipa.entities.AuthenticationUser;
import edu.ucdavis.dss.ipa.entities.UserRole;

/**
 * Component to retrieve the request's current user.
 * 
 * @author christopherthielen
 *
 */
@Component
@Scope(value="request", proxyMode= ScopedProxyMode.TARGET_CLASS)
public class CurrentUser {
	private @Autowired HttpServletRequest request;
	
	/**
	 * Returns the current user. If an impersonating is occurring, it
	 * will return the impersonated user, not the actual user.
	 * 
	 * @return
	 */
	public AuthenticationUser getCurrentUser() {
		if(request.getAttribute("impersonatedUser") != null) {
			return ((AuthenticationUser)request.getAttribute("impersonatedUser"));
		} else {
			return ((AuthenticationUser)request.getAttribute("currentUser"));
		}
	}
	
	/**
	 * Returns true if impersonating.
	 * 
	 * @return
	 */
	public boolean isImpersonating() {
		return request.getAttribute("impersonatedUser") != null;
	}
	
	/**
	 * Shortcut for getCurrentUser.getId();
	 * 
	 * @return
	 */
	public Long getId() {
		if(this.getCurrentUser() != null) {
			return this.getCurrentUser().getId();
		}
		
		return null;
	}
	
	/**
	 * Shortcut for getCurrentUser.getLoginid();
	 * 
	 * @return
	 */
	public String getLoginId() {
		if(this.getCurrentUser() != null) {
			return this.getCurrentUser().getLoginid();
		}
		
		return null;
	}

	/**
	 * Shortcut for getCurrentUser.getDisplayName();
	 * 
	 * @return
	 */
	public String getDisplayName() {
		if(this.getCurrentUser() != null) {
			return this.getCurrentUser().getDisplayName();
		}
		
		return null;
	}

	/**
	 * Returns the actual user in the case of impersonation, i.e.
	 * not the impersonated user but the individual doing the
	 * impersonation.
	 * 
	 * @return
	 */
	public AuthenticationUser getActualUser() {
		return (AuthenticationUser)request.getAttribute("currentUser");
	}

	/**
	 * Shortcut for getCurrentUser().getWorkgroupIds()
	 *
	 * @return
	 */
	public List<Long> getWorkgroupIds() {
		List<Long> workgroupIds = new ArrayList<Long>();

		for(UserRole userRole: this.getCurrentUser().getUserRoles()) {
			workgroupIds.add(userRole.getWorkgroupIdentification());
		}

		return workgroupIds;
	}
}
