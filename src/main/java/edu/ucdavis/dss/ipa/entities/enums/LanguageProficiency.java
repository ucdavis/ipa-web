package edu.ucdavis.dss.ipa.entities.enums;

public enum LanguageProficiency {
    UNDERGRAD(0, "Undergraduate degree from an institution where English is the sole language of instruction"),
    TOEFL(1, "Achieved a minimum score of 26 on the speaking subset of the TOEFL iBT"),
    IELTS(2, "Achieved a minimum score of 8 on the speaking subset of the IELTS"),
    SPEAK(3, "Achieved a minimum score of 50 on the SPEAK"),
    TOEP(4, "Achieved a “Pass” on the TOEP");

    private final long id;
    private final String description;

    LanguageProficiency(long id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static LanguageProficiency getById(long id) {
        for (LanguageProficiency lp: values()) {
            if (lp.id == id) return lp;
        }

        return null;
    }
}
