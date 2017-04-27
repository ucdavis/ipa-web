package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.RestDataWarehouseRepository;
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

    /**
     * Finds Courses with zero units and Queries Data Warehouse for term information and updates the local
     */
    @Scheduled( fixedDelay = 43200000 ) // every 12 hours
    @Async
    public void updateTermsFromDW() {

        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

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
                if (dwCourse.getSubjectCode().equals(course.getSubjectCode())
                && dwCourse.getCourseNumber().equals(course.getCourseNumber())
                && (dwCourse.getCreditHoursHigh() != 0 || dwCourse.getCreditHoursLow() != 0) ) {

                    course.setUnitsHigh(dwCourse.getCreditHoursHigh());
                    course.setUnitsLow(dwCourse.getCreditHoursLow());
                    courseService.update(course);
                }
            }
        }

        runningTask = false;
    }
}
