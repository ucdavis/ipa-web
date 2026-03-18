package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.api.components.budget.views.BudgetExcelView;
import edu.ucdavis.dss.ipa.api.components.budget.views.factories.BudgetViewFactory;
import edu.ucdavis.dss.ipa.entities.BudgetScenario;
import edu.ucdavis.dss.ipa.services.BudgetScenarioService;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


/**
 * Daily dump of latest Budget Scenarios to Box folder
 */
@Service
@Profile({"production", "staging", "development"})
public class UploadBudgetReportTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private final Logger log = LoggerFactory.getLogger("UploadBudgetReportTask");
    @Value("${BOX_UPLOAD_EMAIL:}")
    String boxUploadEmail;
    @Inject
    private BudgetViewFactory budgetViewFactory;
    @Inject
    private BudgetScenarioService budgetScenarioService;
    @Inject
    private EmailService emailService;

    // Run every night at 10pm
    @Scheduled(cron = "0 0 22 ? * MON-FRI", zone = "America/Los_Angeles")
    @Transactional
    public void uploadBudgetReport() {
        if (runningTask) {
            log.debug("uploadBudgetReport() won't run: task already running");
            return;
        } else {
            log.debug("uploadBudgetReport() will run: task not already running");
        }
        runningTask = true;

        if (boxUploadEmail.isBlank()) {
            log.debug("uploadBudgetReport() won't run: BOX_UPLOAD_EMAIL is not configured");
            runningTask = false;
            return;
        }

        long year = Long.parseLong(Year.now().toString());
        String displayYear = String.format("%02d-%02d", year % 100, (year + 1) % 100);

        // get latest budget scenarios for a FY
        List<Long> lsWorkgroupIds =
            List.of(24L, 82L, 18L, 19L, 83L, 64L, 84L, 12L, 81L, 25L, 42L, 41L, 61L, 37L, 36L, 60L, 58L, 48L, 49L, 39L,
                38L, 50L, 51L, 45L, 40L, 16L, 66L, 69L, 46L, 53L, 59L, 65L, 76L, 89L, 54L, 14L, 56L, 17L, 28L, 43L, 78L,
                93L, 94L, 95L, 96L, 97L, 67L, 99L, 100L);

        List<BudgetScenario> latestBudgetRequests = new ArrayList<>();

        for (Long workgroupId : lsWorkgroupIds) {
            List<BudgetScenario> budgetScenarios = budgetScenarioService.findbyWorkgroupIdAndYear(workgroupId, year);

            BudgetScenario latestRequest =
                budgetScenarios.stream().filter(BudgetScenario::getIsBudgetRequest)
                    .max(Comparator.comparing(BudgetScenario::getCreationDate)).orElse(null);
            latestBudgetRequests.add(latestRequest);

        }

        BudgetExcelView budgetReport = budgetViewFactory.createBudgetExcelView(latestBudgetRequests);



        try {
            byte[] budgetReportBytes = budgetReport.toByteArray();


            emailService.send(boxUploadEmail, "Intentionally blank", "Budget Report Upload",
                "Budget Report Download_FY" + displayYear + " Initial.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", budgetReportBytes);
        } catch (Exception e) {
            log.debug("Could not complete uploadBudgetReport()");
            e.printStackTrace();
        } finally {
            runningTask = false;
            log.debug("uploadBudgetReport() completed");
        }
    }
}
