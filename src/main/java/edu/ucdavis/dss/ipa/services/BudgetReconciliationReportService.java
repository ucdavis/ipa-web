package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;

public interface BudgetReconciliationReportService {
    /**
     * Compares the approved budget (standard rates) for a workgroup's academic year against actuals
     * for the matching fiscal year (year + 1), in two views: By Category (Datamart DOPE payroll by
     * instructor type) and By Course (Banner assignments vs planned instructors). Each view is
     * omitted when its source isn't configured.
     *
     * The payroll department scoping the actuals comes from the workgroup's own DepartmentCode. A
     * workgroup without one is outside this report's Letters &amp; Science scope, or unmapped.
     */
    BudgetReconciliationReportView generate(long workgroupId, long year);
}
