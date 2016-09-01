package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Holds the logged in user information extracted from the JWT token.
 * Class is static so that it can be used anywhere.
 * Variables are stored in threads.
 */
public class Authorization {

    private static final ThreadLocal<List<UserRole>> userRoles = new ThreadLocal();
    private static final ThreadLocal<String> loginId = new ThreadLocal();
    private static final ThreadLocal<Long> expirationDate = new ThreadLocal();

    public static void setUserRoles(List<UserRole> userRoles) {
        Authorization.userRoles.set(userRoles);
    }

    public static void setLoginId(String loginId) {
        Authorization.loginId.set(loginId);
    }

    public static String getLoginId() {
        return Authorization.loginId.get();
    }

    public static void setExpirationDate(Long expirationDate) {
        Authorization.expirationDate.set(expirationDate);
    }

    public static Long getExpirationDate() {
        return Authorization.expirationDate.get();
    }

    public static boolean isAdmin() {
        Iterator it = Authorization.userRoles.get().iterator();

        // Iterate over userRoles to find a match
        while (it.hasNext()) {
            LinkedHashMap<String,Map.Entry> pair = (LinkedHashMap<String,Map.Entry>) it.next();

            for (Map.Entry<String, Map.Entry> entry : pair.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals("role") && value.equals("admin")) {
                    return true;
                }
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
    public static boolean hasRole(Long longWorkgroupId, String roleName) {
        if (longWorkgroupId == null || roleName == null) { return false; }
        Integer workgroupId = longWorkgroupId.intValue();

        Iterator it = Authorization.userRoles.get().iterator();

        // Iterate over userRoles to find a match
        while (it.hasNext()) {
            LinkedHashMap<String,Map.Entry> pair = (LinkedHashMap<String,Map.Entry>) it.next();
            Integer userRoleWorkgroupId = null;
            String userRoleRoleName = null;

            for (Map.Entry<String, Map.Entry> entry : pair.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (key.equals("workgroupId")) {
                    userRoleWorkgroupId = (Integer) value;
                }
                if (key.equals("role")) {
                    userRoleRoleName = (String) value;
                }
            }

            // Return true iff both roleName and workgroupId match
            if (roleName.equals(userRoleRoleName) && workgroupId.equals(userRoleWorkgroupId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns a list of workgroups that the user has roles in
     * @return
     */
    public static List<Workgroup> getWorkgroups() {
        List<Workgroup> workgroups = new ArrayList<>();
        for (UserRole userRole: Authorization.userRoles.get()) {
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
    public static int roleCount() {
        return Authorization.userRoles.get().size();
    }
}
