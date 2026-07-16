package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationExcelView;
import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import jakarta.inject.Inject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compares the approved budget against UCPath actuals from the Datamart.
 *
 * Prototype: reads the Datamart directly, so it only exists where DATAMART_URL is set and
 * reachable — not the production web VPC. Production will read stored tallies instead.
 */
@RestController
@Profile({"development"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class BudgetReconciliationReportController {
    @Inject BudgetReconciliationReportService budgetReconciliationReportService;

    /**
     * Mapped under /dev (outside the JWT filter's /api/*) while prototyping; move to /api
     * and restore the authorizer.hasWorkgroupRoles check before this becomes a real screen.
     *
     * @param departmentCode the payroll DEPT_CD for the workgroup, e.g. "040250"
     */
    @RequestMapping(value = "/dev/budgetReconciliationReportView/workgroups/{workgroupId}/years/{year}",
        method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public BudgetReconciliationReportView showReport(@PathVariable long workgroupId, @PathVariable long year,
                                                     @RequestParam("departmentCode") String departmentCode) {
        return budgetReconciliationReportService.generate(workgroupId, year, departmentCode);
    }

    @RequestMapping(value = "/dev/budgetReconciliationReportView/workgroups/{workgroupId}/years/{year}/excel",
        method = RequestMethod.GET)
    public BudgetReconciliationExcelView downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
                                                       @RequestParam("departmentCode") String departmentCode) {
        return new BudgetReconciliationExcelView(
            budgetReconciliationReportService.generate(workgroupId, year, departmentCode));
    }
}
