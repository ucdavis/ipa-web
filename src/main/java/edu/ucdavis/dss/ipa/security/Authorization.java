package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.apache.http.auth.AUTH;

import javax.naming.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the logged in user information extracted from the JWT token.
 * Class is static so that it can be used anywhere.
 * Variables are stored in threads.
 */
public class Authorization {

    private static final ThreadLocal<List<UserRole>> userRoles = new ThreadLocal();
    private static final ThreadLocal<String> loginId = new ThreadLocal();

    public static void setUserRoles(List<UserRole> userRoles) {
        Authorization.userRoles.set(userRoles);
    }

    public static void setLoginId(String loginId) {
        Authorization.loginId.set(loginId);
    }

    public static String getLoginId() {
        return Authorization.loginId.get();
    }

    public static boolean hasRole(String workgroupCode, String roleName) {
        for (UserRole userRole: Authorization.userRoles.get()) {
            if (workgroupCode.equals(userRole.getWorkgroup().getCode())
                && roleName.equals(userRole.getRole().getName())) {
                return true;
            }
        }
        return false;
    }

    public static List<Workgroup> getWorkgroups() {
        List<Workgroup> workgroups = new ArrayList<>();
        for (UserRole userRole: Authorization.userRoles.get()) {
            if (!workgroups.contains(userRole.getWorkgroup())) {
                workgroups.add(userRole.getWorkgroup());
            }
        }
        return workgroups;
    }

}
