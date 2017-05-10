package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.RestDataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UpdateCourseTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

    @Inject RestDataWarehouseRepository restDataWarehouseRepository;
    @Inject CourseService courseService;
    @Inject ActivityService activityService;

    /**
     * Finds Courses with zero units and Queries Data Warehouse for term information and updates the local
     */
    @Scheduled( fixedDelay = 43200000 ) // every 12 hours
    @Async
    public void updateCourseTaskFromDW() {

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        // Update Courses to have the proper units value
        List<Course> courses = this.courseService.getAllCourses();
        Map<String, List<DwCourse>> allDwCourses = new HashMap<>();

        for (Course course : courses) {
            // Only interested in courses with 0 units
            if (course.getUnitsLow() != 0 || course.getUnitsHigh() != 0) {
                continue;
            }

            // Query DW for courses of this subject code, if necessary
            String subjectCode = course.getSubjectCode();
            if (allDwCourses.get(subjectCode) == null) {
                List<DwCourse> dwCourses = restDataWarehouseRepository.queryCourses(subjectCode);
                allDwCourses.put(subjectCode, dwCourses);
            }

            for (DwCourse dwCourse : allDwCourses.get(subjectCode)) {

                // Identify IPA course that matches this dwCourse
                if (dwCourse.getSubjectCode().equals(course.getSubjectCode())
                && dwCourse.getCourseNumber().equals(course.getCourseNumber())) {

                    // Attempt to fill in course units
                    if (dwCourse.getCreditHoursHigh() != 0 || dwCourse.getCreditHoursLow() != 0) {
                        course.setUnitsHigh(dwCourse.getCreditHoursHigh());
                        course.setUnitsLow(dwCourse.getCreditHoursLow());
                        courseService.syncUnits(course);
                    }
                }
            }
        }

        runningTask = false;
    }
}
