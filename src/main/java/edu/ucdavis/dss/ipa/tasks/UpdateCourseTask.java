package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwSearchResultSection;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TermService;
import java.util.ArrayList;
import java.util.List;
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
    @Inject TermService termService;

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
//            long courseYear = course.getYear();
//
//            // returns inexact matches
//            // returns only the latest version of course number
//            List<DwCourse> dwCourses = dataWarehouseRepository.searchCourses(course.getSubjectCode() + " " + course.getCourseNumber())
//                .stream().filter(c -> c.getSubjectCode().equals(course.getSubjectCode()) && c.getCourseNumber().equals(course.getCourseNumber()))
//                .collect(Collectors.toList());
//
//            // make use of sections to update for the year? dataWarehouseRepository.getSectionsBySubjectCodeAndYear
//            // [Courses] Import from Banner works this way
//
//            if (dwCourses.size() > 0) {
//                DwCourse latestDwCourse = dwCourses.stream().sorted(Comparator.comparing(DwCourse::getEffectiveTermCode).reversed()).findFirst().get();
//
//                String latestCourseEffectiveTermCodeTermCode = latestDwCourse.getEffectiveTermCode();
//                Long latestEffectiveYear = termService.getAcademicYearFromTermCode(latestCourseEffectiveTermCodeTermCode);
//
//                Set<String> academicTerms = Term.getTermCodesByYear(courseYear);
//
//                if (latestDwCourse.getCreditHoursLow() != null && course.getUnitsLow() != null) {
//
//
//                    if (Float.compare(latestDwCourse.getCreditHoursLow(), course.getUnitsLow()) !=
//                        0) {
//                        System.out.println("Latest Effective Year != Course Year");
//                        System.out.println(
//                            course.getSubjectCode() + " " + course.getCourseNumber());
//                        System.out.println("Latest version: " +  latestDwCourse.getSubjectCode() + " - " + latestDwCourse.getCourseNumber() + " "+ latestDwCourse.getCreditHoursLow());
//                        System.out.println("Existing version: " + course.getSubjectCode() + " " + course.getCourseNumber() + " - " +course.getUnitsLow());
//                    }
//                }
//            }

            DwCourse dwCourse = dataWarehouseRepository.findCourse(course.getSubjectCode(), course.getCourseNumber(), course.getEffectiveTermCode());

            if (dwCourse != null) {
                courseService.updateUnits(course, dwCourse.getCreditHoursLow(), dwCourse.getCreditHoursHigh());
                numCoursesUpdated++;
            }
        }

        log.debug("updateCoursesFromDW() finished. Updated " + numCoursesUpdated + " courses");

        runningTask = false;
    }

    /**
     * Units are incorrect due to changes over time
     * If course is copied over from existing IPA schedule, an old version might be used
     * Problem does not exist for fresh import from Banner
     * Attempt to update existing courses with proper version based on schedule
     */
    @Scheduled( fixedDelay = ONE_DAY_IN_MILLISECONDS )
    @Async
    public void updateCoursesBySchedule() {
        log.debug("updateCoursesBySchedule() started");

        int incorrectCourseUnits = 0;
        long startTime = System.nanoTime();

        // loop through schedules and find courses
        List<Schedule> currentAndFutureSchedules = scheduleService.findAllCurrentAndFuture();

        for (Schedule schedule : currentAndFutureSchedules) {
            List<Course> courses = courseService.findByScheduleId(schedule.getId());

            // single schedule can have multiple subject codes
            List<String> subjectCodes =
                courses.stream().map(c -> c.getSubjectCode()).distinct().collect(
                    Collectors.toList());

            List<DwSearchResultSection> dwSearchResultSections = new ArrayList<>();
            for (String subjectCode : subjectCodes) {
                dwSearchResultSections.addAll(
                    dataWarehouseRepository.searchImportCourses(subjectCode, schedule.getYear()));
            }

            // compare course against latest DW data
            for (Course course : courses) {
                // need to get sequence pattern for matching
                List<DwSearchResultSection> matchingSections = dwSearchResultSections.stream()
                    .filter(s -> s.getCourseNumber().equals(course.getCourseNumber()) && s.getSequencePattern().equals(course.getSequencePattern()))
                    .collect(Collectors.toList());

                if (matchingSections.size() > 0) {
                    DwSearchResultSection latestSection = matchingSections.stream().findFirst().get();

                    if (course.getUnitsLow() != null) {

                        if (Float.compare(latestSection.getCreditHoursLow(),
                            course.getUnitsLow()) != 0) {
                            incorrectCourseUnits++;
                            System.out.println("Units low mismatch");
                            System.out.println(
                                course.getSubjectCode() + " " + course.getCourseNumber());
                            System.out.println("DW Section: " + latestSection.getCreditHoursLow());
                            System.out.println("IPA Course: " + course.getUnitsLow());
                        }
                    }

                }
            }
        }

        long endTime = System.nanoTime();
        System.out.println("Completed updateCourseBySchedule in " + (endTime - startTime)/1_000_000_000 + "seconds.");
        System.out.println("Courses with mismatched units: " + incorrectCourseUnits);
    }

}
