package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.UserService;

import javax.inject.Inject;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Inject UserService userService;

    Date createdAt, updatedAt;
    String modifiedBy;

    @JsonIgnore
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @JsonIgnore
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /* Hibernate setters */
    @PreUpdate
    private void beforeUpdate() {
        this.updatedAt = new Date();
        String realUserLoginId = Authorization.getRealUserLoginId();
        if (realUserLoginId != null) {
            this.setModifiedBy("user:" + realUserLoginId);
        } else {
            this.setModifiedBy("system");
        }
    }

    @PrePersist
    private void beforeCreation() {
        this.createdAt = new Date();

        String realUserLoginId = Authorization.getRealUserLoginId();
        if (realUserLoginId != null) {
            this.setModifiedBy("user:" + realUserLoginId);
        } else {
            this.setModifiedBy("system");
        }
    }
}