package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.UserService;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jakarta.inject.Inject;
import java.util.*;

@Component
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Authorization {
    private List<UserRole> userRoles;
    private String loginId;
    private String realUserLoginId;
    private Long expirationDate;

    @Inject UserService userService;

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginId() {
        return this.loginId;
    }

    public void setRealUserLoginId(String realUserLoginId) {
        this.realUserLoginId = realUserLoginId;
    }

    public String getRealUserLoginId() {
        return this.realUserLoginId;
    }

    public void setExpirationDate(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Long getExpirationDate() {
        return this.expirationDate;
    }

    public List<UserRole> getUserRoles() {
        if (userRoles != null) {
            return this.userRoles;
        }

        User user = this.userService.getOneByLoginId(this.loginId);
        this.userRoles = user.getUserRoles();

        return this.userRoles;
    }

    public boolean isAdmin() {
        for (UserRole userRole : this.getUserRoles()) {
            if (userRole.getRoleToken().equals("admin")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the user has the given role in the workgroup
     * @param longWorkgroupId
     * @param roleName
     * @return
     */
    public boolean hasRole(Long longWorkgroupId, String roleName) {
        if (longWorkgroupId == null || roleName == null) { return false; }

        for (UserRole userRole : this.getUserRoles()) {
            if (userRole.getWorkgroup() == null) { continue; }

            if (userRole.getWorkgroup().getId() == longWorkgroupId && userRole.getRoleToken().equals(roleName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of workgroups that the user has roles in
     * @return
     */
    public List<Workgroup> getWorkgroups() {
        List<Workgroup> workgroups = new ArrayList<>();
        for (UserRole userRole: this.getUserRoles()) {
            if (!workgroups.contains(userRole.getWorkgroup())) {
                workgroups.add(userRole.getWorkgroup());
            }
        }
        return workgroups;
    }

    /**
     * Returns the number of roles the user has across all workgroups.
     * @return
     */
    public int roleCount() {
        return this.getUserRoles().size();
    }

    public String getUserDisplayName() {
        User user = this.userService.getOneByLoginId(this.loginId);
        if (user != null && user.getDisplayName() != null) {
            return user.getDisplayName();
        } else {
            return user.getFirstName() + " " + user.getLastName();
        }
    }
}
