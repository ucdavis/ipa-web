package edu.ucdavis.dss.ipa.entities.enums;

public enum TermDescription {
    FALL("10"),
    WINTER("01"),
    SPRING("03"),
    SS1("05"),
    SS2("07");

    private final String shortTermCode;

    TermDescription (String shortTermCode) {
        this.shortTermCode = shortTermCode;
    }

    public String getShortTermCode() {
        return shortTermCode;
    }
}
