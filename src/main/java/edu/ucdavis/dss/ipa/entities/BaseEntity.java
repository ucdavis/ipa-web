package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {
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

        // FIXME: We can't inject Authorization into an entity
//        String realUserLoginId = authorizationAttempt.getRealUserLoginId();
//
//        if (realUserLoginId != null) {
//            this.setModifiedBy("user:" + realUserLoginId);
//        } else {
//            this.setModifiedBy("system");
//        }
    }

    @PrePersist
    private void beforeCreation() {
        this.createdAt = new Date();
        this.updatedAt = new Date();

        // FIXME: We can't inject Authorization into an entity
//        String realUserLoginId = authorizationAttempt.getRealUserLoginId();
//
//        if (realUserLoginId != null) {
//            this.setModifiedBy("user:" + realUserLoginId);
//        } else {
//            this.setModifiedBy("system");
//        }
    }
}