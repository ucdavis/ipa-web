package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

@Component
@Scope(scopeName = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Authorization {
    private List<UserRole> userRoles;
    private String loginId;
    private String realUserLoginId;
    private Long expirationDate;

    public void setUserRoles(List<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

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

    public boolean isAdmin() {
        Iterator it = this.userRoles.iterator();

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
    public boolean hasRole(Long longWorkgroupId, String roleName) {
        if (longWorkgroupId == null || roleName == null) { return false; }
        Integer workgroupId = longWorkgroupId.intValue();

        Iterator it = this.userRoles.iterator();

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
    public List<Workgroup> getWorkgroups() {
        List<Workgroup> workgroups = new ArrayList<>();
        for (UserRole userRole: this.userRoles) {
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
        return this.userRoles.size();
    }
}
