package edu.ucdavis.dss.ipa.services.jpa;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import edu.ucdavis.dss.ipa.entities.AuthenticationUser;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
@Transactional
public class JpaAuthenticationService implements AuthenticationService, UserDetailsService {
	@Inject WorkgroupService workgroupService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;

	/**
	 * Called once by Spring Security to authenticate the user.
	 * 
	 * By Spring Security's design, this function may never return null.
	 * Errors are indicated by throwing UsernameNotFoundException.
	 */
	@Override
	public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
		AuthenticationPrincipal authenticationPrincipal = newAuthenticationPrincipal(loginId);
		
		if(authenticationPrincipal == null) {
			throw new UsernameNotFoundException("No such user (" + loginId + ") in the database.");
		}

		return authenticationPrincipal;	
	}

	public AuthenticationPrincipal newAuthenticationPrincipal(String loginId) {
		AuthenticationPrincipal authenticationPrincipal = new AuthenticationPrincipal();
		AuthenticationUser authenticationUser = newAuthenticationUser(loginId);

		authenticationPrincipal.setUser(authenticationUser);

		// Set the first user workgroup as the default
		User user = this.userService.getOneByLoginId(loginId);
		
		if(user != null) {
			List<Workgroup> workgroups = user.getWorkgroups();
			
			Workgroup workgroup = null;
			
			if (workgroups.size() > 0) {
				// Default to the first workgroup found for a user
				workgroup = workgroups.get(0);
			}
			this.setActiveWorkgroup(workgroup, user, authenticationPrincipal);
	
			return authenticationPrincipal;
		} else {
			return null;
		}
	}
	
	public AuthenticationUser newAuthenticationUser(String loginId) {
		User user = userService.getOneByLoginId(loginId);
		Instructor instructor = instructorService.getOneByLoginId(loginId);
		AuthenticationUser authenticationUser = new AuthenticationUser();

		if (user == null) {
			return null;
		} else {
			authenticationUser.setDisplayName(user.getFirstName() + " " + user.getLastName());
			authenticationUser.setUserRoles(user.getUserRoles());
			authenticationUser.setRoles(user.getRoleAssignments());
			authenticationUser.setLoginId(loginId);
			authenticationUser.setId(user.getId());

			if (instructor != null) {
				authenticationUser.setInstructorId(instructor.getId());
			}

			// Update user lastAccessed time
			user.setLastAccessed(new Date());
			this.userService.save(user);

			return authenticationUser;
		}
	}

	@Override
	public AuthenticationUser getCurrentUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();

		boolean impersonating = this.isImpersonating();
		if(impersonating) {
			// Impersonating		
			return principal.getImpersonatedUser();
		} else {
			// Normal Login
			return principal.getUser();
		}	
	}

	@Override
	public void impersonateUser(String loginId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();
		principal.setImpersonatedUser(newAuthenticationUser(loginId));
		
		this.resetActiveWorkgroup();
	}

	@Override
	public void unImpersonateUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();
		principal.setImpersonatedUser(null);
		
		this.resetActiveWorkgroup();
	}
	
	@Override
	public void unsetActiveWorkgroup() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();

		principal.setActiveWorkgroupId(0);
		principal.setActiveWorkgroupName(null);
	}

	@Override
	public AuthenticationUser getActualUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();

		return principal.getUser();
	}

	@Override
	public Boolean isImpersonating() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();

		return principal.getImpersonatedUser() != null;
	}

	@Override
	public void setActiveWorkgroup(Workgroup wg, User user, AuthenticationPrincipal principal) {
		if (wg == null) {
			principal.setActiveWorkgroupId(0L);
			principal.setActiveWorkgroupName(null);
		} else {
			List<Workgroup> workgroups = user.getWorkgroups();
			Workgroup workgroup = this.workgroupService.findOneById(wg.getId());

			if (workgroup != null && workgroups.contains(workgroup)) {
				principal.setActiveWorkgroupId(workgroup.getId());
				principal.setActiveWorkgroupName(workgroup.getName());
			}
		}
	}

	@Override
	public void setActiveWorkgroupForCurrentUser(Workgroup workgroup) {
		User user = this.userService.getOneByLoginId(this.getCurrentUser().getLoginid());
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();

		this.setActiveWorkgroup(workgroup, user, principal);
	}

	@Override
	@Transactional
	public void resetActiveWorkgroup() {
		List<Workgroup> userWorkgroups = this.getCurrentUser().getUserRoles().stream()
				.filter(userRole -> userRole.getWorkgroup() != null) // Filter out null workgroups (Usually Admins have it)
				.map(userRole -> userRole.getWorkgroup())
				.distinct() // Remove duplicates
				.collect(Collectors.toList());

		if (userWorkgroups.size() > 0) {
			this.setActiveWorkgroupForCurrentUser(userWorkgroups.get(0)); // Set the first workgroup in the array as active
		}
		else {
			this.unsetActiveWorkgroup();
		}
	}

	@Override
	public long getActiveWorkgroupId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		AuthenticationPrincipal principal = (AuthenticationPrincipal) auth.getPrincipal();
		
		if (principal.getActiveWorkgroupId() == 0) {
			this.resetActiveWorkgroup();
		}
		
		return principal.getActiveWorkgroupId();
	}

}
