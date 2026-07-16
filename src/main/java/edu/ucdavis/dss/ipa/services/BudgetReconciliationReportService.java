package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;

public interface BudgetReconciliationReportService {
    /**
     * Compares the approved budget (standard rates) for a workgroup's academic year against
     * UCPath actuals from the Datamart for the matching fiscal year (year + 1).
     *
     * @param departmentCode the payroll DEPT_CD the workgroup's payroll lands in, e.g. "040250"
     */
    BudgetReconciliationReportView generate(long workgroupId, long year, String departmentCode);
}
