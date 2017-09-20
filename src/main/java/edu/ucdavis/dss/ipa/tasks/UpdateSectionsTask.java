package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.services.SectionService;
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

    @Inject SectionService sectionService;

    /**
     * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
     */
    @Scheduled( fixedDelay = 86400000 ) // every 24 hours
    @Async
    public void updateSectionsTaskFromDW() {

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        this.sectionService.updateSectionsFromDW();

        runningTask = false;
    }
}
