package edu.ucdavis.dss.ipa.api.components.auth;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.UserRole;
import org.apache.poi.hssf.record.chart.SeriesChartGroupIndexRecord;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = SecurityDTODeserializer.class)
public class SecurityDTO {
    public String token, displayName;
    static public String redirect = null;
    public List<UserRoleDTO> userRoles;
    public List<ScheduleTermState> termStates;

    public SecurityDTO() {
        if(redirect == null) { redirect = "https://cas.ucdavis.edu/cas/login?service=" + SettingsConfiguration.getIpaURL() + "/post-login"; }
    }

    public SecurityDTO(String token) {
        this(); // to set this.redirect
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

    public void setTermStates(List<ScheduleTermState> termStates) {
        this.termStates = termStates;
    }

    public String getRedirect() { return redirect; }
}
