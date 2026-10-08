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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Compares the approved budget against actuals, in two views of the same year: By Category (UCPath
 * DOPE payroll rolled up by instructor type) and By Course (Banner instructional assignments paired
 * with the planned instructors, with DOPE cost attached).
 *
 * Prototype: reads the Datamart and Banner directly, so it only exists where those are set and
 * reachable — not the production web VPC. Production will read stored tallies instead.
 *
 * Mapped under /dev (outside the JWT filter's /api/*) while prototyping; move to /api and restore
 * the authorizer.hasWorkgroupRoles check before this becomes a real screen. The By Course view
 * includes instructor and TA names and pay — treat per IS-3 (individual pay + TA/AI
 * student-employment records).
 */
@RestController
@Profile({"development"})
@ConditionalOnProperty(name = "DATAMART_URL")
public class BudgetReconciliationReportController {
    @Inject BudgetReconciliationReportService budgetReconciliationReportService;

    /**
     * The payroll department scoping the actuals is looked up from the workgroup's code in
     * FteDepartment, so there is no way to pair one department's plan with another's payroll. A
     * workgroup not listed there is a 400 — it is outside this report's Letters &amp; Science scope.
     */
    @RequestMapping(value = "/dev/budgetReconciliationReportView/workgroups/{workgroupId}/years/{year}",
        method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public BudgetReconciliationReportView showReport(@PathVariable long workgroupId, @PathVariable long year) {
        return budgetReconciliationReportService.generate(workgroupId, year);
    }

    @RequestMapping(value = "/dev/budgetReconciliationReportView/workgroups/{workgroupId}/years/{year}/excel",
        method = RequestMethod.GET)
    public BudgetReconciliationExcelView downloadExcel(@PathVariable long workgroupId, @PathVariable long year) {
        return new BudgetReconciliationExcelView(
            budgetReconciliationReportService.generate(workgroupId, year));
    }
}
