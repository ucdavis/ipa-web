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
    BigDecimal actualFte, actualTotalCompensation, actualSalary, summerSalary, comparableSalary,
        difference;

    public BudgetReconciliationCategoryView(String instructorType, boolean includedInComparison,
                                            BigDecimal plannedCost, BigDecimal plannedCount,
                                            int actualPeople, BigDecimal actualFte,
                                            BigDecimal actualTotalCompensation, BigDecimal actualSalary,
                                            BigDecimal summerSalary,
                                            BigDecimal comparableSalary, BigDecimal difference) {
        this.instructorType = instructorType;
        this.includedInComparison = includedInComparison;
        this.plannedCost = plannedCost;
        this.plannedCount = plannedCount;
        this.actualPeople = actualPeople;
        this.actualFte = actualFte;
        this.actualTotalCompensation = actualTotalCompensation;
        this.actualSalary = actualSalary;
        this.summerSalary = summerSalary;
        this.comparableSalary = comparableSalary;
        this.difference = difference;
    }

    public String getInstructorType() {
        return instructorType;
    }

    /** false for context rows (Ladder Faculty, Unmapped) whose difference is not meaningful */
    public boolean isIncludedInComparison() {
        return includedInComparison;
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

    /**
     * Summer Session salary, the amount deducted from actualSalary to reach comparableSalary. Zero on
     * the 12-month faculty and lecturer categories, so actualSalary - summerSalary = comparableSalary
     * holds on every row.
     */
    public BigDecimal getSummerSalary() {
        return summerSalary;
    }

    /** salary compared against plan: full salary less Summer Session pay */
    public BigDecimal getComparableSalary() {
        return comparableSalary;
    }

    public BigDecimal getDifference() {
        return difference;
    }
}
