package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.UserService;

@RestController
public class CourseController {
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject CourseService courseService;
	@Inject DataWarehouseRepository dwRepository;

	@RequestMapping(value = "/api/courses/search/{query}", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<Course> searchCourses (@PathVariable String query) {
		List<Course> courses = new ArrayList<Course>();
		List<DwCourse> dwCourses = new ArrayList<DwCourse>();

		try {
			dwCourses = dwRepository.searchCourses(query);

			for (DwCourse dwCourse : dwCourses) {
				Course course = new Course();
				course.setCourseNumber(dwCourse.getCourseNumber());
				course.setSubjectCode(dwCourse.getSubjectCode());
				course.setEffectiveTermCode(dwCourse.getEffectiveTermCode());
				course.setTitle(dwCourse.getTitle());
				courses.add(course);

				// Limit to 20 results (consider moving this logic to DW, probably by adding another param for limit)
				int RESULTS_LIMIT = 20;
				if (courses.size() >= RESULTS_LIMIT) break;
			}
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}

		return courses;
	}
	
	@RequestMapping(value = "/api/courses/{id}", method = RequestMethod.PUT)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Course updateCourse(@PathVariable Long id, @RequestBody Course newCourse, HttpServletResponse httpResponse) {
		Course course = courseService.findOneById(id);

		if (course == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		course.setCourseNumber(newCourse.getCourseNumber());
		course.setEffectiveTermCode(newCourse.getEffectiveTermCode());
		course.setCourseOverlaps(newCourse.getCourseOverlaps());
		return this.courseService.saveCourse(course);
	}

	@RequestMapping(value = "/api/courses/{courseId}/courseOverlaps/{courseOverlapsId}", method = RequestMethod.PUT)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Course addTeachingOverlap(@PathVariable Long courseId, @PathVariable Long courseOverlapsId, HttpServletResponse httpResponse) {
		Course course = courseService.findOneById(courseId);
		Course courseOverlap = courseService.findOneById(courseOverlapsId);

		if (course == null || courseOverlap == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		List<Course> courseOverlaps = course.getCourseOverlaps();		
		courseOverlaps.add(courseOverlap);
		return this.courseService.saveCourse(course);
	}
	
	@RequestMapping(value = "/api/courses/{courseId}/courseOverlaps/{courseOverlapsId}", method = RequestMethod.DELETE)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Course removeCourseOverlap(@PathVariable Long courseId, @PathVariable Long courseOverlapsId, HttpServletResponse httpResponse) {
		Course course = courseService.findOneById(courseId);
		Course courseOverlap = courseService.findOneById(courseOverlapsId);

		if (course == null || courseOverlap == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		List<Course> courseOverlaps = course.getCourseOverlaps();		
		courseOverlaps.remove(courseOverlap);
		return this.courseService.saveCourse(course);
	}

	@RequestMapping(value = "/api/courses", method = RequestMethod.POST)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public Course addCourse(@RequestBody Course newCourse, HttpServletResponse httpResponse) {
		Course course = courseService.findOrCreateByEffectiveTermAndSubjectCodeAndCourseNumberAndTitle(newCourse.getEffectiveTermCode(), newCourse.getSubjectCode(), newCourse.getCourseNumber(), newCourse.getTitle());

		if (course == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		return course;
	}
}