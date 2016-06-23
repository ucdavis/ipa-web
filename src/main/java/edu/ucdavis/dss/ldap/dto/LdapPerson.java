package edu.ucdavis.dss.ldap.dto;

/**
 * Created by okadri on 6/23/16.
 */
public class LdapPerson {
    private long personId;
    private String mail, first, last, ucdPersonUUID, telephoneNumber, loginId, street, employeeNumber, ucdStudentSid, ucdPersonPIDM, displayName;

    public long getPersonId() {
        return this.personId;
    }

    public void setPersonId(long personId) {
        this.personId = personId;
    }

    public String getUcdPersonUUID() {
        return ucdPersonUUID;
    }

    public void setUcdPersonUUID(String ucdPersonUUID) {
        this.ucdPersonUUID = ucdPersonUUID;
    }

    public String getUcdPersonPIDM() {
        return ucdPersonPIDM;
    }

    public void setUcdPersonPIDM(String ucdPersonPIDM) {
        this.ucdPersonPIDM = ucdPersonPIDM;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getUcdStudentSid() {
        return ucdStudentSid;
    }

    public void setUcdStudentSid(String ucdStudentSid) {
        this.ucdStudentSid = ucdStudentSid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
