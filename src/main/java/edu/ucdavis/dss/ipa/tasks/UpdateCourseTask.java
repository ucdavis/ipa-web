package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.*;

@Service
@Profile({"production", "staging", "development"})
public class UpdateCourseTask {
    final long ONE_DAY_IN_MILLISECONDS = 86400000;
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private final Logger log = LoggerFactory.getLogger("UpdateCourseTask");

    @Inject DataWarehouseRepository dataWarehouseRepository;
    @Inject CourseService courseService;
    @Inject ScheduleService scheduleService;

    /**
     * Finds courses with null units low (does not occur in Banner) and queries for updates.
     * (Could this also happen if a course is added by the user but it does not yet
     * exist in Banner, e.g. user is working in a future term?)
     *
     * There was also a historical bug where UnitsLow was erroneously set to zero for
     * some courses. This _is_ allowable in Banner but represents a very small percentage
     * of courses in any given term, so we double-check zero unit courses as well.
     */
    @Scheduled( fixedDelay = ONE_DAY_IN_MILLISECONDS )
    @Async
    public void updateCoursesFromDW() {
        int numCoursesUpdated = 0;

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateCoursesFromDW() started");

        // Update Courses to have the proper units value

        // UnitsLow should not be null, so check these
        List<Course> courses = this.courseService.findByUnitsLow(null);

        // UnitsLow rarely equals 0 and we had a historical bug, so check these for a while
        // (remove this one after 2018-04-13)
        courses.addAll(this.courseService.findByUnitsLow(0f));

        // Any current or future schedule should be checked to ensure we're up-to-date
        // with any Banner changes
        List<Schedule> currentAndFutureSchedules = this.scheduleService.findAllCurrentAndFuture();
        List<Course> currentAndFutureCourses = this.courseService.findByScheduleIn(currentAndFutureSchedules);

        courses.addAll(currentAndFutureCourses);

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
