package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
@Profile({"production", "staging"})
public class UpdateSectionsTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private static final Logger log = LoggerFactory.getLogger("UpdateSectionsTask");

    @Inject ScheduleOpsService scheduleOpsService;

    /**
     * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
     */
    @Scheduled( fixedDelay = 86400000 ) // every 24 hours
    @Async
    public void updateSectionsTaskFromDW() {

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        this.scheduleOpsService.updateSectionsFromDW();
        try {
            this.scheduleOpsService.updateEmptySectionGroups();
        } catch (org.springframework.transaction.TransactionSystemException e) {
            log.error("TransactionSystemException while updating empty section groups!");
            log.error(e.toString());
        }

        runningTask = false;
    }
}
