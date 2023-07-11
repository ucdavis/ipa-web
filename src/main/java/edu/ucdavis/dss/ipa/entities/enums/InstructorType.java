package edu.ucdavis.dss.ipa.entities.enums;

public enum InstructorType {
    EMERITI(1, "Emeriti - Recalled"),
    VISITING_PROFESSOR(2, "Visiting Professor"),
    ASSOCIATE_INSTRUCTOR(3, "Associate Instructor"),
    UNIT18_LECTURER(4, "Unit 18 Pre-Six Lecturer"),
    CONTINUING_LECTURER(5, "Continuing Lecturer"),
    LADDER_FACULTY(6, "Ladder Faculty"),
    INSTRUCTOR(7, "Instructor"),
    LECTURER_SOE(8, "Lecturer SOE"),
    NEW_FACULTY_HIRE(9, "New Faculty Hire"),
    CONTINUING_LECTURER_AUGMENTATION(10, "Continuing Lecturer - Augmentation");

    private final long id;
    private final String description;

    InstructorType(long id, String description) {
        this.id = id;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
