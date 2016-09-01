package edu.ucdavis.dss.ipa.security.authorization;

import edu.ucdavis.dss.ipa.security.Authorization;
import org.springframework.security.access.AccessDeniedException;

/**
 * Created by okadri on 6/22/16.
 */
public interface Authorizer<T> {
    public void authorize(T entity, Object... args);

    /**
     * Throws an exception if there is no user logged in or user has no roles and is not admin.
     * Useful if you just want to authorize anyone with IPA access.
     */
    public static void isAuthorized() {
        if(Authorization.isAdmin() == false) {
            if(Authorization.roleCount() <= 0) {
                throw new AccessDeniedException("User not authorized at all.");
            }
        }
    }

    /**
     * Verifies that the user has the role for the workgroup or is an admin
     * @param workgroupId
     * @param roleName
     */
    public static void hasWorkgroupRole(Long workgroupId, String roleName) {
        if (Authorization.isAdmin() == false && Authorization.hasRole(workgroupId, roleName) == false) {
            throw new AccessDeniedException("User not authorized to create tags in workgroup with Id = " + workgroupId);
        }
    };

    /**
     * Verifies that the user has at least one of the roles for the workgroup or is an admin
     * @param workgroupId
     * @param roleNames
     */
    public static void hasWorkgroupRoles(Long workgroupId, String... roleNames) {
        if (Authorization.isAdmin()) { return; }

        for (String roleName: roleNames) {
            if (Authorization.hasRole(workgroupId, roleName)) {
                return;
            }
        }

        throw new AccessDeniedException("User not authorized to create tags in workgroup with Id = " + workgroupId);
    };
}
