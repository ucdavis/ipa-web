package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.validation.Email;
import edu.ucdavis.dss.ipa.api.views.InstructorViews;
import edu.ucdavis.dss.ipa.api.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.api.views.TeachingCallResponseViews;

/**
 * This entity is similar to the instructor, but for instructionalSupport assignments.
 * The expectation is that these will never be deleted and will serve as data points for historical records.
 */

@SuppressWarnings("serial")
@Entity
@Table(name = "InstructionalSupportStaff")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InstructionalSupportStaff implements Serializable {
    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private String loginId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId()
    {
        return this.id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    @Basic
    @Column(name = "FirstName", nullable = false, length = 45)
    @JsonProperty
    public String getFirstName()
    {
        return this.firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "LastName", nullable = false, length = 45)
    @JsonProperty
    public String getLastName()
    {
        return this.lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    @JsonProperty
    @Transient
    public String getFullName()
    {
        return this.firstName + " " + this.lastName;
    }

    @Basic
    @Column(name = "Email", nullable = true, length = 45, unique = true)
    @JsonProperty("emailAddress")
    @Email
    public String getEmail()
    {
        return this.email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    @Basic
    @Column(name = "LoginId", nullable = true, length = 45, unique = true)
    @JsonProperty
    @JsonView({TeachingCallResponseViews.Detailed.class, SectionGroupViews.Detailed.class, InstructorViews.Detailed.class})
    public String getLoginId()
    {
        return this.loginId;
    }

    public void setLoginId(String loginId)
    {
        this.loginId = loginId;
    }
}