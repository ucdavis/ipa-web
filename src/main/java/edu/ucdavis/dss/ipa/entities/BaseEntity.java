package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.ucdavis.dss.ipa.config.AutowireHelper;
import edu.ucdavis.dss.ipa.security.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.RequestContextHolder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    Date createdAt, updatedAt;
    String modifiedBy;

    @Autowired Authorization authorization;

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
        String realUserLoginId = null;

        // Only attempt to auto-wire if we're in a request. We may be in a background
        // task, in which case the authorization will fail as it uses request-scoped
        // beans.
        if(RequestContextHolder.getRequestAttributes() != null) {
            AutowireHelper.autowire(this, this.authorization);
            realUserLoginId = authorization.getRealUserLoginId();
        }

        if (realUserLoginId != null) {
            this.setModifiedBy("user:" + realUserLoginId);
        } else {
            this.setModifiedBy("system");
        }
    }

    @PrePersist
    private void beforeCreation() {
        String realUserLoginId = null;

        this.createdAt = new Date();
        this.updatedAt = new Date();

        // Only attempt to auto-wire if we're in a request. We may be in a background
        // task, in which case the authorization will fail as it uses request-scoped
        // beans.
        if(RequestContextHolder.getRequestAttributes() != null) {
            AutowireHelper.autowire(this, this.authorization);
            realUserLoginId = authorization.getRealUserLoginId();
        }

        if (realUserLoginId != null) {
            this.setModifiedBy("user:" + realUserLoginId);
        } else {
            this.setModifiedBy("system");
        }
    }
}