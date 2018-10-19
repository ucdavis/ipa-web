package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
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
public class UpdateSectionsTask {
    final long SIX_HOURS_IN_MILLISECONDS = 21600000;
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private static final Logger log = LoggerFactory.getLogger("UpdateSectionsTask");

    @Inject ScheduleOpsService scheduleOpsService;
    @Inject CourseService courseService;

    /**
     * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
     */
    @Scheduled( fixedDelay = SIX_HOURS_IN_MILLISECONDS )
    @Async
    public void updateSectionsFromDW() {
        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateSectionsFromDW() started");

        List<Course> courses = this.courseService.getAllCourses();

        for (Course course : courses) {
            this.scheduleOpsService.updateSectionsByCourseFromDW(course);
        }

        try {
            this.scheduleOpsService.updateEmptySectionGroups();
        } catch (org.springframework.transaction.TransactionSystemException e) {
            log.error("TransactionSystemException while updating empty section groups!");
            log.error(e.toString());
        }

        log.debug("updateSectionsFromDW() finished");

        runningTask = false;
    }
}
