package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.services.SectionService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UpdateSectionsTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

    @Inject
    SectionService sectionService;
    /**
     * Finds Courses with zero units and Queries Data Warehouse for term information and updates the local
     */
    @Scheduled( fixedDelay = 43200000 ) // every 12 hours
    @Async
    public void updateSectionsTaskFromDW() {

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        this.sectionService.updateSectionsFromDW();

        runningTask = false;
    }
}
