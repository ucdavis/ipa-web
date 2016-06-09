package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.CourseOfferingService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.components.term.views.TermCourseOfferingView;
import edu.ucdavis.dss.ipa.api.components.term.views.factories.TermViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.CourseOfferingViews;

@RestController
public class CourseOfferingController {
	@Inject CourseOfferingService courseOfferingService;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject TeachingCallViewFactory teachingCallViewFactory;
	@Inject TermViewFactory termViewFactory;
	@Inject ScheduleService scheduleService;
	@Inject CurrentUser currentUser;

	@RequestMapping(value = "/api/courseOfferings", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(CourseOfferingViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public CourseOffering updateCourseOfferingSeatsTotal (
			@RequestBody CourseOffering co,
			HttpServletResponse httpResponse) {
		CourseOfferingGroup cog = this.courseOfferingGroupService.getCourseOfferingGroupById(co.getCourseOfferingGroup().getId());
		if (cog == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		ScheduleTermState sts = this.scheduleTermStateService.createScheduleTermState(cog.getSchedule(), co.getTermCode());

		if (sts == null || sts.scheduleTermLocked()) {
			httpResponse.setStatus(HttpStatus.LOCKED.value());
			return null;
		} else if (co.getSeatsTotal() == null) {
			// Delete co if the setasTotal is empty
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
			UserLogger.log(currentUser, "Deleted '" + cog.getDescription() + "' courseOffering for term " + co.getTermCode());
			this.courseOfferingService.deleteByCourseOfferingGroupAndTermCode(cog, co.getTermCode());
			return null;
		} else {
			httpResponse.setStatus(HttpStatus.OK.value());
			CourseOffering newCo = this.courseOfferingService.findOrCreateOneByCourseOfferingGroupAndTermCode(cog, co.getTermCode());
			UserLogger.log(currentUser, "Updated '" + cog.getDescription() + "' courseOffering seats for term " + co.getTermCode() + " from " + newCo.getSeatsTotal() + " to " + co.getSeatsTotal());
			newCo.setSeatsTotal(co.getSeatsTotal());
			return this.courseOfferingService.saveCourseOffering(newCo);
		}
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')"
			+ "or hasPermission(#scheduleId, 'schedule', 'senateInstructor') or hasPermission(#scheduleId, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/terms/{termCode}/courseOfferings", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<TermCourseOfferingView> getTermViewCourseOfferingsByScheduleAndTermCode(
			@PathVariable long scheduleId,
			@PathVariable String termCode,
			HttpServletResponse httpResponse) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		httpResponse.setStatus(HttpStatus.OK.value());
		return this.termViewFactory.createTermCourseOfferingsView(schedule, termCode);
	}
}
