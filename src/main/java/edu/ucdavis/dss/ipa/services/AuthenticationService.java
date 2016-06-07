package edu.ucdavis.dss.ipa.services;
import org.springframework.security.core.userdetails.UserDetails;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.AuthenticationUser;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;

public interface AuthenticationService {

	AuthenticationUser getCurrentUser();

	AuthenticationUser getActualUser();

	void impersonateUser(String loginId);

	void unImpersonateUser();

	Boolean isImpersonating();	

	void setActiveWorkgroup(Workgroup workgroup, User user, AuthenticationPrincipal principal);

	void setActiveWorkgroupForCurrentUser(Workgroup workgroup);

	void resetActiveWorkgroup();

	long getActiveWorkgroupId();

	UserDetails loadUserByUsername(String loginId);

	void unsetActiveWorkgroup();

}
