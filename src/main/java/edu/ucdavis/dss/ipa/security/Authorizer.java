package edu.ucdavis.dss.ipa.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Helper functions to perform authorization checks in controllers, etc.
 */
@Service
public class Authorizer {
    @Inject Authorization authorization;

    /**
     * Throws an exception if there is no user logged in or user has no roles and is not admin.
     * Useful if you just want to authorize anyone with IPA access.
     */
    public void isAuthorized() {
        if(authorization.isAdmin() == false) {
            if(authorization.roleCount() <= 0) {
                throw new AccessDeniedException("User not authorized at all.");
            }
        }
    }

    /**
     * Throws an exception if user is not admin.
     * Useful if you just want to authorize admins.
     */
    public void isAdmin() {
        if(authorization.isAdmin() == false) {
            throw new AccessDeniedException("User not authorized. Admins only.");
        }
    }

    public void isDeansOffice() {
        if (authorization.isAdmin() == false && authorization.getUserRoles().stream().anyMatch(ur -> ur.getRoleToken().equals("deansOffice")) == false) {
            throw new AccessDeniedException("User not authorized. Dean's Office only.");
        }
    }

    /**
     * Verifies that the user has the role for the workgroup or is an admin
     * @param workgroupId
     * @param roleName
     */
    public void hasWorkgroupRole(Long workgroupId, String roleName) {
        if (authorization.isAdmin() == false && authorization.hasRole(workgroupId, roleName) == false) {
            throw new AccessDeniedException("User not authorized for workgroup with Id = " + workgroupId);
        }
    };

    /**
     * Verifies that the user has at least one of the roles for the workgroup or is an admin
     * @param workgroupId
     * @param roleNames
     */
    public void hasWorkgroupRoles(Long workgroupId, String... roleNames) {
        if (authorization.isAdmin()) { return; }

        for (String roleName: roleNames) {
            if (authorization.hasRole(workgroupId, roleName)) {
                return;
            }
        }

        throw new AccessDeniedException("User not authorized for workgroup with Id = " + workgroupId);
    };

    public String getLoginId(){
        return authorization.getLoginId();
    }

    public String getUserDisplayName() {return authorization.getUserDisplayName();}
}
