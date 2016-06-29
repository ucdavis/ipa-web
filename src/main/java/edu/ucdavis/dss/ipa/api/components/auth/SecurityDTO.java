package edu.ucdavis.dss.ipa.api.components.auth;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.ipa.entities.UserRole;

import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = SecurityDTODeserializer.class)
public class SecurityDTO {
    public String token;
    public String redirect = "https://cas.ucdavis.edu/cas/login?service=http://localhost:8080/post-login";
    public List<UserRoleDTO> userRoles;

    public SecurityDTO() {
    }

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
}
