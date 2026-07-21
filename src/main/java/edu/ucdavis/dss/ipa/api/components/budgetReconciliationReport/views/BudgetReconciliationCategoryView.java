package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One instructor category compared between the approved budget and UCPath actuals.
 */
public class BudgetReconciliationCategoryView {
    String instructorType;
    boolean includedInComparison;
    BigDecimal plannedCost, plannedCount;
    Integer plannedPeople, plannedPlaceholders, bannerTaAssignments, bannerTaIndividuals;
    int actualPeople;
    BigDecimal actualFte, actualTotalCompensation, actualSalary, actualJulSepCompensation,
        comparableSalary, variance;

    public BudgetReconciliationCategoryView(String instructorType, boolean includedInComparison,
                                            BigDecimal plannedCost, BigDecimal plannedCount,
                                            Integer plannedPeople, Integer plannedPlaceholders,
                                            Integer bannerTaAssignments, Integer bannerTaIndividuals,
                                            int actualPeople, BigDecimal actualFte,
                                            BigDecimal actualTotalCompensation, BigDecimal actualSalary,
                                            BigDecimal actualJulSepCompensation,
                                            BigDecimal comparableSalary, BigDecimal variance) {
        this.instructorType = instructorType;
        this.includedInComparison = includedInComparison;
        this.plannedCost = plannedCost;
        this.plannedCount = plannedCount;
        this.plannedPeople = plannedPeople;
        this.plannedPlaceholders = plannedPlaceholders;
        this.bannerTaAssignments = bannerTaAssignments;
        this.bannerTaIndividuals = bannerTaIndividuals;
        this.actualPeople = actualPeople;
        this.actualFte = actualFte;
        this.actualTotalCompensation = actualTotalCompensation;
        this.actualSalary = actualSalary;
        this.actualJulSepCompensation = actualJulSepCompensation;
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

    public BigDecimal getPlannedCost() {
        return plannedCost;
    }

    public BigDecimal getPlannedCount() {
        return plannedCount;
    }

    /**
     * Distinct named instructors planned in this category (one person may teach several courses);
     * comparable to actualPeople. Null where not assignment-based (TAs/Readers). Excludes
     * type-only placeholder assignments, which name no person.
     */
    public Integer getPlannedPeople() {
        return plannedPeople;
    }

    /**
     * Type-only placeholder assignments planned in this category — course slots assigned a type
     * but no named person. Counted per assignment. Null where not assignment-based (TAs/Readers).
     */
    public Integer getPlannedPlaceholders() {
        return plannedPlaceholders;
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

    public BigDecimal getActualJulSepCompensation() {
        return actualJulSepCompensation;
    }

    /** salary compared against plan: excludes Jul-Sep (Summer Session) for TA/AI/Reader categories */
    public BigDecimal getComparableSalary() {
        return comparableSalary;
    }

    public BigDecimal getVariance() {
        return variance;
    }
}
