package edu.ucdavis.dss.ipa.api.components.assignment;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;

@RestController
@CrossOrigin
public class AssignmentViewCourseController {
    @Inject CurrentUser currentUser;
    @Inject AuthenticationService authenticationService;
    @Inject WorkgroupService workgroupService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;

    @RequestMapping(value = "/api/assignmentView/{workgroupId}/{year}/courses", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<Course> getCoursesByWorkgroupIdAndYear(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return this.courseService.findByWorkgroupIdAndYear(workgroupId, year);
    }
}

