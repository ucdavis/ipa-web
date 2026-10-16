package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * The budget scenario's own bottom line, so the reconciliation ties back to the scenario total rather
 * than a subtotal of the compared categories. totalTeachingCost decomposes as the per-category costs
 * on the By Category tab (Ladder Faculty included), plus uncategorizedInstructorCost, plus expenses.
 *
 * Funds and balance are deliberately absent: calculateTermTotals sums every line item into TOTAL_FUNDS
 * without filtering on term, while this report covers Fall/Winter/Spring only. Term-filter the funds
 * before reporting them here.
 */
public class PlannedTotalsView {
    BigDecimal uncategorizedInstructorCost;
    BigDecimal expenses;
    BigDecimal totalTeachingCost;

    public PlannedTotalsView(BigDecimal uncategorizedInstructorCost, BigDecimal expenses,
                             BigDecimal totalTeachingCost) {
        this.uncategorizedInstructorCost = uncategorizedInstructorCost;
        this.expenses = expenses;
        this.totalTeachingCost = totalTeachingCost;
    }

    /**
     * Planned instructor cost that landed in no reported category. Normally zero; non-zero means the
     * By Category tab is not showing the whole plan.
     */
    public BigDecimal getUncategorizedInstructorCost() {
        return uncategorizedInstructorCost;
    }

    /** non-instructional expense items on the scenario, which the scenario folds into teaching cost */
    public BigDecimal getExpenses() {
        return expenses;
    }

    public BigDecimal getTotalTeachingCost() {
        return totalTeachingCost;
    }
}
