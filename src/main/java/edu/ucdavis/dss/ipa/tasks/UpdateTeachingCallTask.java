package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Periodically check teachingCallReceipts to determine if any need to be locked.
 * This includes teaching calls set to lock after due date AND forms that need to be re-locked after 7 days.
 */

@Service
@Profile({"production", "staging", "development"})
public class UpdateTeachingCallTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private final Logger log = LoggerFactory.getLogger("UpdateTeachingCallTask");
    @Inject
    TeachingCallReceiptService teachingCallReceiptService;

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Los_Angeles")
    public void updateTeachingCallStatus() {
        if (runningTask) {
            return;
        }
        runningTask = true;

        log.debug("updateTeachingCallStatus() started");

        teachingCallReceiptService.lockExpiredReceipts();

        runningTask = false;
        log.debug("updateTeachingCallStatus() finished");
    }
}
