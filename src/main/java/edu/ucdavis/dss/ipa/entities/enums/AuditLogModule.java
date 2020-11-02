package edu.ucdavis.dss.ipa.entities.enums;

public enum AuditLogModule {
    COURSES("Courses"),
    ASSIGN_INSTRUCTORS("Assign Instructors"),
    BUDGET("Budget");

    private final String description;

    AuditLogModule (String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }
}
