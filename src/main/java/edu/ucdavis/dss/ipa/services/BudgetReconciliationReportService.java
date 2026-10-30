package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;

public interface BudgetReconciliationReportService {
    /**
     * Compares the approved budget (standard rates) for a workgroup's academic year against actuals
     * for the matching fiscal year (year + 1), in two views: By Category (Datamart DOPE payroll by
     * instructor type) and By Course (Banner assignments vs planned instructors).
     *
     * The payroll department scoping the actuals is looked up from the workgroup's code in
     * FteDepartment. A workgroup not listed there is outside this report's Letters &amp; Science scope.
     */
    BudgetReconciliationReportView generate(long workgroupId, long year);
}
