package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "AuditLog")
public class AuditLog extends BaseEntity{
    private long id;
    private String message;
    private String userName;
    private String loginId;
    private long year;
    private Workgroup workgroup;
    private String module;
    private UUID transactionId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "Message", unique = false, nullable = false)
    @JsonProperty
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Basic
    @Column(name = "UserName", unique = false, nullable = false)
    @JsonProperty
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Basic
    @Column(name = "LoginId", unique = false, nullable = false)
    @JsonProperty
    public String getLoginId() {
        return this.loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    @Basic
    @Column(name = "Year", unique = false, nullable = false)
    @JsonProperty
    public long getYear() {
        return this.year;
    }

    public void setYear(long year) {
        this.year = year;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WorkgroupId", nullable = false)
    @NotNull
    @JsonIgnore
    public Workgroup getWorkgroup() {
        return this.workgroup;
    }

    public void setWorkgroup(Workgroup workgroup)
    {
        this.workgroup = workgroup;
    }

    @Basic
    @Column(name = "Module", unique = false, nullable = false)
    @JsonProperty
    public String getModule() {
        return this.module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    @Basic
    @Column(name = "TransactionId", unique = false, nullable = false, columnDefinition = "BINARY(16)")
    @JsonProperty
    public UUID getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    @JsonProperty("createdOn")
    @Transient
    public Date getCreatedOn() {
        return createdAt;
    }
}
