package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport;

import java.util.List;

/**
 * A payroll department (DOPE DEPT_CD) in the reconciliation's scope and the programs paid under it.
 * Programs sharing a code are reported together, because DOPE cannot split them.
 */
public record FteDepartment(String payrollCode, String name, List<String> programs) {
    public static final List<FteDepartment> ALL = List.of(
        new FteDepartment("040004", "Classics", List.of("CLA")),
        new FteDepartment("040010", "American Studies", List.of("AMS")),
        new FteDepartment("040013", "Design", List.of("DES")),
        new FteDepartment("040020", "Anthropology", List.of("ANT")),
        new FteDepartment("040025", "East Asian Languages & Cultures", List.of("CHN", "JPN")),
        new FteDepartment("040027", "Middle East/South Asia Studies",
            List.of("ARB", "HEB", "HIN", "MSA", "PER", "PUN")),
        new FteDepartment("040030", "Art & Art History", List.of("AHI", "ART")),
        new FteDepartment("040050", "African American & African Studies", List.of("AAS")),
        new FteDepartment("040064", "Chicana/o Studies", List.of("CHI")),
        new FteDepartment("040070", "Chemistry", List.of("CHE")),
        new FteDepartment("040075", "Comparative Literature", List.of("COM")),
        new FteDepartment("040100", "Theatre & Dance", List.of("DRA")),
        new FteDepartment("040110", "Economics", List.of("ECN")),
        new FteDepartment("040130", "English", List.of("ENL")),
        new FteDepartment("040135", "University Writing Program", List.of("UWP")),
        new FteDepartment("040140", "French & Italian", List.of("FRE", "ITA")),
        new FteDepartment("040160", "Earth & Planetary Sciences", List.of("EPS")),
        new FteDepartment("040170", "German & Russian", List.of("GER", "RUS")),
        new FteDepartment("040180", "History", List.of("HIS")),
        new FteDepartment("040185", "Humanities", List.of("HUM")),
        new FteDepartment("040210", "Linguistics", List.of("LIN")),
        new FteDepartment("040220", "Mathematics", List.of("MAT")),
        new FteDepartment("040225", "Medieval & Early Modern Studies", List.of("MEMS")),
        new FteDepartment("040240", "Music", List.of("MUS")),
        new FteDepartment("040250", "Philosophy", List.of("PHI")),
        new FteDepartment("040255", "Science & Technology Studies", List.of("STS")),
        new FteDepartment("040270", "Physics & Astronomy", List.of("PHY")),
        new FteDepartment("040280", "Political Science", List.of("POL")),
        new FteDepartment("040290", "Psychology", List.of("PSC")),
        new FteDepartment("040300", "Religious Studies", List.of("RST")),
        new FteDepartment("040301", "Human Rights", List.of("HMR")),
        new FteDepartment("040310", "Communication", List.of("CMN")),
        new FteDepartment("040320", "Sociology", List.of("SOC")),
        new FteDepartment("040330", "Spanish & Portuguese", List.of("POR", "SPA")),
        new FteDepartment("040331", "Cinema & Digital Media", List.of("CDM")),
        new FteDepartment("040370", "Asian American Studies", List.of("ASA")),
        new FteDepartment("040375", "Native American Studies", List.of("NAS")),
        new FteDepartment("040380", "Gender, Sexuality & Women's Studies", List.of("GSW")),
        new FteDepartment("040420", "Statistics", List.of("STA")));

    public static FteDepartment forWorkgroupCode(String workgroupCode) {
        return ALL.stream()
            .filter(fteDepartment -> fteDepartment.programs().contains(workgroupCode))
            .findFirst()
            .orElse(null);
    }
}
