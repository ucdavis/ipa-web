package edu.ucdavis.dss.ipa.api.components.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.ucdavis.dss.ipa.entities.UserRole;

public class UserRoleDTO {

    @JsonProperty
    public long workgroupId = 0;

    @JsonProperty
    public String roleName = "", workgroupName = "";

    public UserRoleDTO() {
    }

    public UserRoleDTO(UserRole userRole) {
        if (userRole == null || userRole.getRole() == null) {
            return;
        }

        this.roleName = userRole.getRole().getName();

        if (userRole.getWorkgroup() != null) {
            this.workgroupId = userRole.getWorkgroup().getId();
            this.workgroupName = userRole.getWorkgroup().getName();
        }
    }
}