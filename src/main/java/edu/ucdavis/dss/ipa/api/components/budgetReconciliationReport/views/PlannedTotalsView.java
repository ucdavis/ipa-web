package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.math.BigDecimal;

/**
 * The budget scenario's own bottom line, so the reconciliation ties back to the page the Dean's
 * Office approved rather than to a subtotal of the compared categories alone.
 *
 * totalTeachingCost is the scenario's figure as the budget UI shows it, and it decomposes as: the
 * per-category costs on the By Category tab (compared categories plus context Ladder Faculty),
 * plus uncategorizedInstructorCost, plus expenses.
 *
 * Funds and balance are deliberately absent. calculateTermTotals sums every non-hidden line item into
 * TOTAL_FUNDS without filtering on term, while this report scopes cost to Fall/Winter/Spring, so a
 * scenario carrying summer line items would show those funds against academic-year cost. Term-filter
 * the funds before reporting them here.
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
     * Planned instructor cost that landed in no reported category — an assignment whose instructor
     * type resolved to none of the ten. Normally zero; non-zero means the By Category tab is not
     * showing the whole plan.
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
