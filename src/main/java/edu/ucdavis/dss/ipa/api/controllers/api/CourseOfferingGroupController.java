package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.annual.views.AnnualCourseOfferingGroupView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.CourseOfferingGroupViews;

@RestController
public class CourseOfferingGroupController {
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	@Inject ScheduleService scheduleService;
	@Inject AuthenticationService authenticationService;
	@Inject TeachingCallViewFactory teachingCallViewFactory;
	@Inject CurrentUser currentUser;

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(CourseOfferingGroupViews.Detailed.class)
	public Course getCourseOfferingGroupExpandedDetails(@PathVariable Long id, Model model) {
		return this.courseOfferingGroupService.getCourseOfferingGroupById(id);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public AnnualCourseOfferingGroupView updateCourseOfferingGroup(
			@RequestBody Course cogDto,
			@PathVariable Long id,
			HttpServletResponse httpResponse) {
		Course cog = this.courseOfferingGroupService.getCourseOfferingGroupById(id);

		if (cog != null && this.scheduleService.isScheduleClosed(cog.getSchedule().getId())) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		} else {
			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Renamed courseOfferingGroup with ID " + id + " from " + cog.getTitle() + " to " + cogDto.getTitle());
			cog = this.courseOfferingGroupService.setCourseSubject(id, cogDto.getTitle());
		}

		return new AnnualCourseOfferingGroupView(cog);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedule/{scheduleId}/courseOfferingGroups", method = RequestMethod.POST)
	@ResponseBody
	public AnnualCourseOfferingGroupView addSectionGroup(@RequestBody Course course, @PathVariable Long scheduleId, HttpServletResponse httpResponse) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if (scheduleService.isScheduleClosed(schedule.getId())) {
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return null;
		}

		Course newCog = this.courseOfferingGroupService.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);

		httpResponse.setStatus(HttpStatus.OK.value());
		UserLogger.log(currentUser, "Created new courseOfferingGroup '" + newCog.getDescription() + "' schedule with ID " + scheduleId);
		return new AnnualCourseOfferingGroupView(newCog);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteCourseOfferingGroup(@PathVariable Long id, HttpServletResponse httpResponse) {
		Course cog = this.courseOfferingGroupService.getCourseOfferingGroupById(id);
		if (cog != null) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
			UserLogger.log(currentUser, "Deleted courseOfferingGroup '" + cog.getDescription() + "' with ID " + id);

			// TODO: the logic to decide to delete a COG should exist on a public interface
			// And the actual deletion of the COG should be a private method in the service layer
			// Workgroup, Schedule, and COGs can all be deleted, but entering from COG deletion
			// Requires consideration of additional business logic
			if (this.scheduleService.isScheduleClosed(cog.getSchedule().getId())) {
				httpResponse.setStatus(HttpStatus.LOCKED.value());
			} else {
				this.courseOfferingGroupService.deleteCourseOfferingGroupById(id);
				httpResponse.setStatus(HttpStatus.OK.value());
			}
		} else {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		}
	}


	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}/tracks", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(CourseOfferingGroupViews.Summary.class)
	public AnnualCourseOfferingGroupView updateCourseOfferingGroupTracks(@RequestBody List<Tag> tags, @PathVariable Long id,
																		 HttpServletResponse httpResponse) {
		Course course = this.courseOfferingGroupService.getCourseOfferingGroupById(id);

		if (course != null && this.scheduleService.isScheduleClosed(course.getSchedule().getId())) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		} else {
			Workgroup workgroup = course.getSchedule().getWorkgroup();

			for(Tag tag : tags) {
				tag.setWorkgroup(workgroup);
			}
			course.setTags(tags);

			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Updated tags for course '" + course.getDescription() + "' with ID " + id);
			course = this.courseOfferingGroupService.saveCourseOfferingGroup(course);
		}

		return new AnnualCourseOfferingGroupView(course);
	}
}