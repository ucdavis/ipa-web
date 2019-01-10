package edu.ucdavis.dss.ipa.api.components.assignment;

import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.web.bind.annotation.*;

@RestController
public class AssignmentViewCourseController {
    @Inject CourseService courseService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/assignmentView/{workgroupId}/{year}/courses", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Course> getCoursesByWorkgroupIdAndYear(@PathVariable long workgroupId, @PathVariable long year) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return this.courseService.findByWorkgroupIdAndYear(workgroupId, year);
    }
}

