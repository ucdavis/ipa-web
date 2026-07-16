package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * One instructor category compared between the approved budget and UCPath actuals.
 */
public class BudgetReconciliationCategoryView {
    String instructorType;
    boolean includedInComparison;
    BigDecimal plannedCost, plannedCount;
    int actualPeople;
    BigDecimal actualFte, actualTotalCompensation, actualSalary, actualJulSepCompensation,
        comparableSalary, variance;

    public BudgetReconciliationCategoryView(String instructorType, boolean includedInComparison,
                                            BigDecimal plannedCost, BigDecimal plannedCount,
                                            int actualPeople, BigDecimal actualFte,
                                            BigDecimal actualTotalCompensation, BigDecimal actualSalary,
                                            BigDecimal actualJulSepCompensation,
                                            BigDecimal comparableSalary, BigDecimal variance) {
        this.instructorType = instructorType;
        this.includedInComparison = includedInComparison;
        this.plannedCost = plannedCost;
        this.plannedCount = plannedCount;
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
