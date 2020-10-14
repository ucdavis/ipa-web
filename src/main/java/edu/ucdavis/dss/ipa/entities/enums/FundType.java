package edu.ucdavis.dss.ipa.entities.enums;

public enum FundType {
    TOTAL(0, "Total"),
    DEANS_OFFICE(1, "Funds From Dean's Office"),
    INTERNAL_BUYOUT(2, "Internal Buyout"),
    CLASS_CANCELLED(3, "Class Cancelled - Funds no longer needed"),
    RANGE_ADJUSTMENT(4, "Range Adjustment Funds"),
    WORK_LIFE(5, "Work-Life Balance Funds"),
    OTHER(6, "Other Funds"),
    EXTERNAL_BUYOUT(7, "External Buyout"),
    ADDITIONAL_DEANS_OFFICE(8, "Additional Funds From Dean's Office"),
    NOT_GENT(9, "Funds not in GENT Account");

    private final long id;
    private final String description;

    FundType(long id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static FundType getById(long id) {
        for (FundType f : values()) {
            if (f.id == id) return f;
        }

        return null;
    }
}
