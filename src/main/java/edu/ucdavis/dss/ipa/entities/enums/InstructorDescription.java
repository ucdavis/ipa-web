package edu.ucdavis.dss.ipa.entities.enums;

public enum InstructorDescription {
    EMERITI(1),
    VISITING_PROFESSOR(2),
    ASSOCIATE_PROFESSOR(3),
    UNIT18_LECTURER(4),
    CONTINUING_LECTURER(5),
    LADDER_FACULTY(6),
    INSTRUCTOR(7),
    LECTURER_SOE(8),
    NEW_FACULTY_HIRE(9),
    CONTINUING_LECTURER_AUGMENTATION(10);

    private final long typeId;

    InstructorDescription (long typeId) {
        this.typeId = typeId;
    }

    public long typeId() {
        return typeId;
    }
}
