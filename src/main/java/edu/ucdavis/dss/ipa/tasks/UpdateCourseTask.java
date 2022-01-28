package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwSearchResultSection;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    @Scheduled (fixedDelay = ONE_DAY_IN_MILLISECONDS )
    @Async
    public void updateCoursesFromDW() {
        final long SYNC_START_YEAR = 2016L; // IPA release year
        int numCoursesUpdated = 0;

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateCoursesFromDW() started");

        /**
         * Ensure we're up-to-date with any Banner changes
         * Course can change year over year, if a course schedule is copied over from existing IPA data,
         * an old version (effectiveTermCode) is used. This is not a problem when doing a course import from Banner
         * Attempt to reconcile existing IPA courses with proper version based on schedule year.
         */

        int incorrectEffectiveTermCode = 0;
        int incorrectUnits = 0;
        long startTime = System.nanoTime();

        // Loop courses by schedule to make sure we check against the correct year of the course as the existing effective term may be incorrect.
        List<Schedule> schedules =
            scheduleService.findAll().stream().filter(s -> s.getYear() > SYNC_START_YEAR).collect(Collectors.toList());

        for (Schedule schedule : schedules) {
            List<Course> scheduleCourses = courseService.findByScheduleId(schedule.getId());

            // Schedule can have more than one subject code
            List<String> scheduleSubjectCodes =
                scheduleCourses.stream().map(Course::getSubjectCode).distinct().collect(Collectors.toList());

            for (String subjectCode : scheduleSubjectCodes) {
                List<Course> subjectCodeCourses = scheduleCourses.stream()
                    .filter(c -> subjectCode.equals(c.getSubjectCode()))
                    .collect(Collectors.toList());
                List<DwSearchResultSection> dwCourseImportSections =
                    dataWarehouseRepository.searchSections(subjectCode, schedule.getYear());

                for (Course course : subjectCodeCourses) {
                    List<DwSearchResultSection> courseSections = dwCourseImportSections.stream()
                        .filter(s -> s.getCourseNumber().equals(course.getCourseNumber()) &&
                            s.getSequencePattern().equals(course.getSequencePattern()))
                        .collect(Collectors.toList());

                    if (courseSections.size() > 0) {
                        // ignoring Summer sections if Fall or later is available?
//                        DwSearchResultSection firstSection = courseSections.stream()
//                            .filter(s -> s.getTermCode().compareTo(TermDescription.FALL.getTermCode(schedule.getYear())) >= 0)
//                            .findFirst().orElseGet(() -> courseSections.get(0));

                        // Sections are returned in order from Summer to Spring
                        DwSearchResultSection firstSection = courseSections.get(0);

                        if (course.getEffectiveTermCode().compareTo(firstSection.getEffectiveTermCode()) != 0) {
                            incorrectEffectiveTermCode++;
                            System.out.println("*** Effective term code mismatch ***");

                            System.out.println(course.getSubjectCode() + " " + course.getCourseNumber());
                            System.out.println("Schedule id: " + schedule.getId() + " year: " + schedule.getYear());
                            System.out.println("IPA Course ID: " + course.getId());
                            courseService.updateFromDwSearchResultSection(course, firstSection);
                            numCoursesUpdated++;

                            if (course.getUnitsLow() != null &&
                                Float.compare(course.getUnitsLow(), firstSection.getCreditHoursLow()) != 0) {
                                incorrectUnits++;
//                                System.out.println("+++ Credit Hours Low Mismatch +++");
//                                System.out.println("Updating IPA Course with DW Section");
//                                System.out.println("DwSection term" + firstSection.getTermCode());
//                                System.out.println("DwSection title" + firstSection.getTitle());
//                                System.out.println(
//                                    "Setting effective term code from " + course.getEffectiveTermCode() + " to " +
//                                        firstSection.getEffectiveTermCode());
//                                System.out.println("Setting unit low from " + course.getUnitsLow() + " to " +
//                                    firstSection.getCreditHoursLow());
//                                System.out.println("Setting unit high from " + course.getUnitsHigh() + " to " +
//                                    firstSection.getCreditHoursHigh());
//
//                                courseService.updateFromDwSearchResultSection(course, firstSection);
                            }
                        }

                    }
                }
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Completed updateCourseBySchedule in " +
            TimeUnit.SECONDS.convert(endTime - startTime, TimeUnit.NANOSECONDS) + " seconds.");

        System.out.println("Courses with mismatched effectiveTermCode: " + incorrectEffectiveTermCode);
        System.out.println("Courses with mismatched units: " + incorrectUnits);

        log.debug("updateCoursesFromDW() finished. Updated " + numCoursesUpdated + " courses");

        runningTask = false;
    }
}
