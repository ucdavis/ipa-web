package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One instructor category compared between the approved budget and UCPath actuals.
 */
public class BudgetReconciliationCategoryView {
    String instructorType;
    boolean includedInComparison, summerBearing;
    BigDecimal plannedCost, plannedCount;
    Integer bannerTaAssignments, bannerTaIndividuals;
    int actualPeople;
    BigDecimal actualFte, actualTotalCompensation, actualSalary, actualJulSepCompensation,
        actualJulSepSalary, comparableSalary, variance;

    public BudgetReconciliationCategoryView(String instructorType, boolean includedInComparison,
                                            boolean summerBearing,
                                            BigDecimal plannedCost, BigDecimal plannedCount,
                                            Integer bannerTaAssignments, Integer bannerTaIndividuals,
                                            int actualPeople, BigDecimal actualFte,
                                            BigDecimal actualTotalCompensation, BigDecimal actualSalary,
                                            BigDecimal actualJulSepCompensation,
                                            BigDecimal actualJulSepSalary,
                                            BigDecimal comparableSalary, BigDecimal variance) {
        this.instructorType = instructorType;
        this.includedInComparison = includedInComparison;
        this.summerBearing = summerBearing;
        this.plannedCost = plannedCost;
        this.plannedCount = plannedCount;
        this.bannerTaAssignments = bannerTaAssignments;
        this.bannerTaIndividuals = bannerTaIndividuals;
        this.actualPeople = actualPeople;
        this.actualFte = actualFte;
        this.actualTotalCompensation = actualTotalCompensation;
        this.actualSalary = actualSalary;
        this.actualJulSepCompensation = actualJulSepCompensation;
        this.actualJulSepSalary = actualJulSepSalary;
        this.comparableSalary = comparableSalary;
        this.variance = variance;
    }

    public String getInstructorType() {
        return instructorType;
    }

    /** false for context rows (Ladder Faculty, Unmapped) whose variance is not meaningful */
    public boolean isIncludedInComparison() {
        return includedInComparison;
    }

    /**
     * True for the categories whose regular pay starts in October (TAs, Associate Instructor,
     * Readers), so their Jul-Sep salary is treated as Summer Session and dropped from
     * comparableSalary. False everywhere else, where Jul-Sep pay is kept.
     */
    public boolean isSummerBearing() {
        return summerBearing;
    }

    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    /**
     * The plan's count field for this category: course assignments for the faculty and lecturer
     * categories, but TAs' and Readers' own units, which are not courses.
     */
    public BigDecimal getPlannedCount() {
        return plannedCount;
    }

    /**
     * Actual TA assignment rows from Banner (each TA-section-term appointment counts once) for the
     * academic year. Set only on the TAs row; null elsewhere and when Banner is not configured.
     * Not comparable to actualPeople — one TA teaching several sections counts multiple times here.
     */
    public Integer getBannerTaAssignments() {
        return bannerTaAssignments;
    }

    /**
     * Distinct TA individuals from Banner (by PIDM) for the academic year. Set only on the TAs row.
     * Roughly comparable to actualPeople, but counts by course subject vs actualPeople's payroll dept.
     */
    public Integer getBannerTaIndividuals() {
        return bannerTaIndividuals;
    }

    public int getActualPeople() {
        return actualPeople;
    }

    public BigDecimal getActualFte() {
        return actualFte;
    }

    public BigDecimal getActualTotalCompensation() {
        return actualTotalCompensation;
    }

    public BigDecimal getActualSalary() {
        return actualSalary;
    }

    /** Jul-Sep pay, salary and benefits together. Context: only the salary part is ever deducted. */
    public BigDecimal getActualJulSepCompensation() {
        return actualJulSepCompensation;
    }

    /**
     * The salary-only part of Jul-Sep pay — the figure actually subtracted from actualSalary to get
     * comparableSalary, but only where summerBearing is true. Reported on every category so a
     * non-summer-bearing row still shows what it kept.
     */
    public BigDecimal getActualJulSepSalary() {
        return actualJulSepSalary;
    }

    /** salary compared against plan: excludes Jul-Sep (Summer Session) for TA/AI/Reader categories */
    public BigDecimal getComparableSalary() {
        return comparableSalary;
    }

    public BigDecimal getVariance() {
        return variance;
    }
}
