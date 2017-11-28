package edu.ucdavis.dss.ipa.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * Created by okadri on 6/22/16.
 */
@Service
public class Authorizer {
    @Inject
    Authorization authorizationAttempt;

    /**
     * Throws an exception if there is no user logged in or user has no roles and is not admin.
     * Useful if you just want to authorize anyone with IPA access.
     */
    public void isAuthorized() {
        if(authorizationAttempt.isAdmin() == false) {
            if(authorizationAttempt.roleCount() <= 0) {
                throw new AccessDeniedException("User not authorized at all.");
            }
        }
    }

    /**
     * Throws an exception if user is not admin.
     * Useful if you just want to authorize admins.
     */
    public void isAdmin() {
        if(authorizationAttempt.isAdmin() == false) {
            throw new AccessDeniedException("User not authorized. Admins only.");
        }
    }

    /**
     * Verifies that the user has the role for the workgroup or is an admin
     * @param workgroupId
     * @param roleName
     */
    public void hasWorkgroupRole(Long workgroupId, String roleName) {
        if (authorizationAttempt.isAdmin() == false && authorizationAttempt.hasRole(workgroupId, roleName) == false) {
            throw new AccessDeniedException("User not authorized for workgroup with Id = " + workgroupId);
        }
    };

    /**
     * Verifies that the user has at least one of the roles for the workgroup or is an admin
     * @param workgroupId
     * @param roleNames
     */
    public void hasWorkgroupRoles(Long workgroupId, String... roleNames) {
        if (authorizationAttempt.isAdmin()) { return; }

        for (String roleName: roleNames) {
            if (authorizationAttempt.hasRole(workgroupId, roleName)) {
                return;
            }
        }

        throw new AccessDeniedException("User not authorized for workgroup with Id = " + workgroupId);
    };
}
