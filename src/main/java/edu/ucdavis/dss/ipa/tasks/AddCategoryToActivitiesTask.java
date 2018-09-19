package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
@Profile({"production", "staging", "development"})
public class AddCategoryToActivitiesTask {
    final long ONE_DAY_IN_MILLISECONDS = 86400000;
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private static final Logger log = LoggerFactory.getLogger("UpdateUsersTask");

    @Inject DataWarehouseRepository dataWarehouseRepository;
    @Inject ActivityService activityService;
    @Inject ScheduleService scheduleService;

    /**
     * Scans all activities, and fills in isPrimary flag as appropriate
     */
    @Scheduled( fixedDelay = ONE_DAY_IN_MILLISECONDS )
    @Async
    public void updateActivitiesFromDw() {
        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateActivitiesFromDW() started");

        List<Schedule> schedules = this.scheduleService.findAll();


        for (Schedule schedule : schedules) {
            // Get a subjectCode from first course in schedule
            // If this subjectCode was not already queried, query that year + subject, and make a hash with keys of 'subj + number + activityType'

            // Loop through a courses sections/activities in that course
            // Look in 'isPrimary' hash for already found answer based on 'subj + number + activityType'
        }

        log.debug("updateUsersFromDW() finished");

        runningTask = false;
    }
}
