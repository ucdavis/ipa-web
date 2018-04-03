package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
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
public class UpdateCourseTask {
    final long ONE_DAY_IN_MILLISECONDS = 86400000;
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private final Logger log = LoggerFactory.getLogger("UpdateCourseTask");

    @Inject DataWarehouseRepository dataWarehouseRepository;
    @Inject CourseService courseService;

    /**
     * Finds courses with null units low (should not happen) and queries for updates
     */
    @Scheduled( fixedDelay = ONE_DAY_IN_MILLISECONDS )
    @Async
    public void updateCoursesFromDW() {
        int numCoursesUpdated = 0;

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateCoursesFromDW() started");

        // Update Courses to have the proper units value
        List<Course> courses = this.courseService.findByUnitsLow(null);

        for (Course course : courses) {
            DwCourse dwCourse = dataWarehouseRepository.findCourse(course.getSubjectCode(), course.getCourseNumber(), course.getEffectiveTermCode());

            if (dwCourse != null) {
                courseService.updateUnits(course, dwCourse.getCreditHoursLow(), dwCourse.getCreditHoursHigh());
                numCoursesUpdated++;
            }
        }

        log.debug("updateCoursesFromDW() finished. Updated " + numCoursesUpdated + " courses");

        runningTask = false;
    }
}
