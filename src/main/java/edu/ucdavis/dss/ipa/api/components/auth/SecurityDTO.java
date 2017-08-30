package edu.ucdavis.dss.ipa.api.components.auth;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.UserRole;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = SecurityDTODeserializer.class)
public class SecurityDTO {
    public String token, displayName, realUserDisplayName, loginId, realUserLoginId;
    static public String redirect = null;
    public List<UserRoleDTO> userRoles;
    public List<ScheduleTermState> termStates;

    public SecurityDTO() { }

    public SecurityDTO(String token) {
        this.token = token;
    }

    public void setUserRoles(List<UserRole> userRoles) {
        if (userRoles != null) {

            this.userRoles = new ArrayList<UserRoleDTO>();

            for (UserRole userRole : userRoles) {
                this.userRoles.add(new UserRoleDTO(userRole));
            }
        }
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public void setRealUserLoginId(String realUserLoginId) {
        this.realUserLoginId = realUserLoginId;
    }

    public void setTermStates(List<ScheduleTermState> termStates) {
        this.termStates = termStates;
    }

    public String getRedirect() { return redirect; }

    public void setRedirect(String redirect) { this.redirect = redirect; }

    public void setRealUserDisplayName(String realUserDisplayName) {
        this.realUserDisplayName = realUserDisplayName;
    }
}
