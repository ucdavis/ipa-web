package edu.ucdavis.dss.ipa.web.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.web.components.annual.views.AnnualCourseOfferingGroupView;
import edu.ucdavis.dss.ipa.web.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.web.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.web.views.CourseOfferingGroupViews;

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
	public CourseOfferingGroup getCourseOfferingGroupExpandedDetails(@PathVariable Long id, Model model) {
		return this.courseOfferingGroupService.getCourseOfferingGroupById(id);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public AnnualCourseOfferingGroupView updateCourseOfferingGroup(
			@RequestBody CourseOfferingGroup cogDto,
			@PathVariable Long id,
			HttpServletResponse httpResponse) {
		CourseOfferingGroup cog = this.courseOfferingGroupService.getCourseOfferingGroupById(id);

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

		CourseOfferingGroup newCog = this.courseOfferingGroupService.createCourseOfferingGroupByCourseAndScheduleId(scheduleId, course);

		httpResponse.setStatus(HttpStatus.OK.value());
		UserLogger.log(currentUser, "Created new courseOfferingGroup '" + newCog.getDescription() + "' schedule with ID " + scheduleId);
		return new AnnualCourseOfferingGroupView(newCog);
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteCourseOfferingGroup(@PathVariable Long id, HttpServletResponse httpResponse) {
		CourseOfferingGroup cog = this.courseOfferingGroupService.getCourseOfferingGroupById(id);
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
	public AnnualCourseOfferingGroupView updateCourseOfferingGroupTracks( @RequestBody List<Track> tracks, @PathVariable Long id, 
			HttpServletResponse httpResponse) {
		CourseOfferingGroup courseOfferingGroup = this.courseOfferingGroupService.getCourseOfferingGroupById(id);

		if (courseOfferingGroup != null && this.scheduleService.isScheduleClosed(courseOfferingGroup.getSchedule().getId())) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
		} else {
			Workgroup workgroup = courseOfferingGroup.getSchedule().getWorkgroup();

			for(Track track : tracks) {
				track.setWorkgroup(workgroup);
			}
			courseOfferingGroup.setTracks(tracks);

			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Updated tracks for courseOfferingGroup '" + courseOfferingGroup.getDescription() + "' with ID " + id);
			courseOfferingGroup = this.courseOfferingGroupService.saveCourseOfferingGroup(courseOfferingGroup);
		}

		return new AnnualCourseOfferingGroupView(courseOfferingGroup);
	}
}