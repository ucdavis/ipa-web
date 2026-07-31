package edu.ucdavis.dss.banner.dto;

/**
 * One instructional assignment row from Banner BANINST1.ZIVASGN: a person assigned to a course
 * (subject + course number) in a term with a functional category (FCTG_CODE: FA, AI, NF, TA).
 */
public class BannerAssignment {
    private String termCode;
    private String subjectCode;
    private String courseNumber;
    private String functionalCategory;
    private String pidm;
    private String emplId;
    private String lastName;
    private String firstName;
    private String middleInitial;

    public String getTermCode() {
        return termCode;
    }

    public void setTermCode(String termCode) {
        this.termCode = termCode;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    /** ZIVASGN_FCTG_CODE: FA (faculty), AI (associate instructor), TA, NF (unconfirmed). */
    public String getFunctionalCategory() {
        return functionalCategory;
    }

    public void setFunctionalCategory(String functionalCategory) {
        this.functionalCategory = functionalCategory;
    }

    public String getPidm() {
        return pidm;
    }

    public void setPidm(String pidm) {
        this.pidm = pidm;
    }

    /** UCPath employee id via GENERAL.WOBEUCD (crosswalk from PIDM); null if the person has no row */
    public String getEmplId() {
        return emplId;
    }

    public void setEmplId(String emplId) {
        this.emplId = emplId;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    /**
     * "First Last". The middle initial is deliberately left out (2026-07-29) so this matches how IPA
     * renders an instructor — Instructor.getFullName() is firstName + lastName and IPA holds no middle
     * name — which makes the planned and actual columns of the By Course tab visually comparable.
     * getMiddleInitial() still exposes it if anything ever needs to disambiguate two people.
     */
    public String getFullName() {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.isBlank()) {
            name.append(firstName.trim()).append(' ');
        }
        if (lastName != null && !lastName.isBlank()) {
            name.append(lastName.trim());
        }
        return name.toString().trim();
    }
}
