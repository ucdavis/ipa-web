package edu.ucdavis.dss.ipa.api.components.staffingByCourseReport;

import edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views.BudgetReconciliationReportView;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.StaffingByCourseExcelView;
import edu.ucdavis.dss.ipa.api.components.staffingByCourseReport.views.StaffingByCourseReportView;
import edu.ucdavis.dss.ipa.services.BudgetReconciliationReportService;
import edu.ucdavis.dss.ipa.services.StaffingByCourseReportService;
import jakarta.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Planned (IPA) vs actual (Banner) staffing per course. Prototype: reads Banner directly, so it
 * only exists where BANNER_DATABASE_URL is set. Subjects are derived from the budget scenario;
 * departmentCode is the payroll DEPT_CD used to pull DOPE cost (name-matched onto instructors),
 * attached only when the Datamart is also configured.
 *
 * Mapped under /dev (outside the JWT filter) while prototyping; restore auth before it becomes a
 * real screen. Output includes instructor and TA names and pay — treat per IS-3 (individual pay +
 * TA/AI student-employment records).
 */
@RestController
@Profile({"development"})
@ConditionalOnProperty(name = "BANNER_DATABASE_URL")
public class StaffingByCourseReportController {
    @Inject StaffingByCourseReportService staffingByCourseReportService;
    /* summary tab; only present when the Datamart is configured */
    @Autowired(required = false) BudgetReconciliationReportService budgetReconciliationReportService;

    @RequestMapping(value = "/dev/staffingByCourseView/workgroups/{workgroupId}/years/{year}",
        method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public StaffingByCourseReportView showReport(@PathVariable long workgroupId, @PathVariable long year,
                                                 @RequestParam(value = "departmentCode", required = false)
                                                 String departmentCode) {
        return staffingByCourseReportService.generate(workgroupId, year, departmentCode);
    }

    @RequestMapping(value = "/dev/staffingByCourseView/workgroups/{workgroupId}/years/{year}/excel",
        method = RequestMethod.GET)
    public StaffingByCourseExcelView downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
                                                   @RequestParam(value = "departmentCode", required = false)
                                                   String departmentCode) {
        StaffingByCourseReportView staffing =
            staffingByCourseReportService.generate(workgroupId, year, departmentCode);

        // summary tab needs both a department and the Datamart-backed reconciliation service
        BudgetReconciliationReportView reconciliation =
            budgetReconciliationReportService != null && departmentCode != null && !departmentCode.isBlank()
                ? budgetReconciliationReportService.generate(workgroupId, year, departmentCode)
                : null;

        return new StaffingByCourseExcelView(staffing, reconciliation);
    }
}
