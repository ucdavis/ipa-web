package edu.ucdavis.dss.ipa.api.components.assignment;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.views.SectionGroupViews;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallByCourseView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallByInstructorView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallSectionGroupView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.TeachingCallSummaryView;
import edu.ucdavis.dss.ipa.api.components.assignment.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import org.springframework.web.servlet.View;

@RestController
public class AssignmentViewCourseController {
    @Inject CurrentUser currentUser;
    @Inject AuthenticationService authenticationService;
    @Inject WorkgroupService workgroupService;
    @Inject ScheduleService scheduleService;
    @Inject CourseService courseService;

    @RequestMapping(value = "/api/assignments/{workgroupId}/{year}/courses", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @JsonView(SectionGroupViews.Detailed.class)
    public List<Course> getCoursesByWorkgroupIdAndYear(
            @PathVariable long id,
            @PathVariable long workgroupId,
            @PathVariable long year,
            HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "senateInstructor", "federationInstructor");
        return this.courseService.findByWorkgroupIdAndYear(id, year);
    }
}

