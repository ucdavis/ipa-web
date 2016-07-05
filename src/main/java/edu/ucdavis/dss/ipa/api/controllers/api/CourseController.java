package edu.ucdavis.dss.ipa.api.controllers.api;

import edu.ucdavis.dss.ipa.api.components.assignment.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.components.course.views.AnnualCourseView;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Tag;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.utilities.UserLogger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class CourseController {
	@Inject
	CourseService courseService;
	@Inject ScheduleService scheduleService;
	@Inject AuthenticationService authenticationService;
	@Inject
	TeachingCallViewFactory teachingCallViewFactory;
	@Inject CurrentUser currentUser;

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public Course getCourseOfferingGroupExpandedDetails(@PathVariable Long id, Model model) {
		return this.courseService.getOneById(id);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public AnnualCourseView updateCourseOfferingGroup(
			@RequestBody Course courseDto,
			@PathVariable Long id,
			HttpServletResponse httpResponse) {
		Course course = this.courseService.getOneById(id);

		if (course != null && this.scheduleService.isScheduleClosed(course.getSchedule().getId())) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		} else {
			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Renamed courseOfferingGroup with ID " + id + " from " + course.getTitle() + " to " + courseDto.getTitle());
			course.setTitle(courseDto.getTitle());
			this.courseService.save(course);
		}

		return new AnnualCourseView(course);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedule/{scheduleId}/courseOfferingGroups", method = RequestMethod.POST)
	@ResponseBody
	public AnnualCourseView addSectionGroup(@RequestBody Course course, @PathVariable Long scheduleId, HttpServletResponse httpResponse) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if (scheduleService.isScheduleClosed(schedule.getId())) {
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return null;
		}

		course = this.courseService.save(course);

		httpResponse.setStatus(HttpStatus.OK.value());
		UserLogger.log(currentUser, "Created new course '" + course.getTitle() + "' schedule with ID " + scheduleId);
		return new AnnualCourseView(course);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteCourseOfferingGroup(@PathVariable Long id, HttpServletResponse httpResponse) {
		Course course = this.courseService.getOneById(id);
		if (course != null) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
			UserLogger.log(currentUser, "Deleted course '" + course.getTitle() + "' with ID " + id);

			// TODO: the logic to decide to delete a COG should exist on a public interface
			// And the actual deletion of the COG should be a private method in the service layer
			// Workgroup, Schedule, and COGs can all be deleted, but entering from COG deletion
			// Requires consideration of additional business logic
			if (this.scheduleService.isScheduleClosed(course.getSchedule().getId())) {
				httpResponse.setStatus(HttpStatus.LOCKED.value());
			} else {
				this.courseService.delete(id);
				httpResponse.setStatus(HttpStatus.OK.value());
			}
		} else {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		}
	}


	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}/tracks", method = RequestMethod.POST)
	@ResponseBody
	public AnnualCourseView updateCourseOfferingGroupTracks(@RequestBody List<Tag> tags, @PathVariable Long id,
															HttpServletResponse httpResponse) {
		Course course = this.courseService.getOneById(id);

		if (course != null && this.scheduleService.isScheduleClosed(course.getSchedule().getId())) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		} else {
			Workgroup workgroup = course.getSchedule().getWorkgroup();

			for(Tag tag : tags) {
				tag.setWorkgroup(workgroup);
			}
			course.setTags(tags);

			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Updated tags for course '" + course.getTitle() + "' with ID " + id);
			course = this.courseService.save(course);
		}

		return new AnnualCourseView(course);
	}
}