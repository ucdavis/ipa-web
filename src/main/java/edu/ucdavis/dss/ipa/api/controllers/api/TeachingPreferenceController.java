package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingPreference;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.CourseOfferingService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TeachingPreferenceService;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallTeachingPreferenceView;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.SectionViews;
import edu.ucdavis.dss.ipa.api.views.TeachingPreferenceViews;

@RestController
public class TeachingPreferenceController {
	@Inject TeachingPreferenceService teachingPreferenceService;
	@Inject AuthenticationService authenticationService;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;
	@Inject SectionGroupService sectionGroupService;
	@Inject CourseOfferingService courseOfferingService;
	@Inject CurrentUser currentUser;
	@Inject CourseService courseService;

	@RequestMapping(value = "/api/teachingPreferences/{id}", method = RequestMethod.GET)
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public TeachingPreference teachingPreferenceById(@PathVariable Long id) {
		return this.teachingPreferenceService.findOneById(id);
	}

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')")
	@RequestMapping(value = "/api/instructors/{instructorId}/teachingPreferences", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public List<TeachingPreference> getInstructorTeachingPreferencesScheduleId(
			@PathVariable long instructorId, HttpServletResponse httpResponse,
			@RequestParam(value = "teachingCallId", required = true) long teachingCallId) {
		return teachingPreferenceService.getTeachingPreferencesByTeachingCallIdAndInstructorId(teachingCallId, instructorId);
	}

	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingPreferences", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public List<TeachingPreference> teachingPreferencesByScheduleIdOrTermCode(@PathVariable long scheduleId,
			@RequestParam(value = "termCode", required = false) String termCode) {
		if (termCode != null && !termCode.isEmpty()) {
			return teachingPreferenceService.getTeachingPreferencesByScheduleIdAndTermCode(scheduleId, termCode);
		} else {
			return teachingPreferenceService.getTeachingPreferencesByScheduleId(scheduleId);
		}
	}
	
	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator') or hasPermission(#scheduleId, 'schedule', 'senateInstructor') or hasPermission(#scheduleId, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/schedules/{scheduleId}/teachingPreferences", method = RequestMethod.POST)
	@ResponseBody
	public TeachingCallTeachingPreferenceView createTeachingPreference(
			@PathVariable Long scheduleId,
			@RequestBody TeachingPreference teachingPreference,
			HttpServletResponse httpResponse) {

		Schedule schedule = this.scheduleService.findById(scheduleId);
		CourseOffering courseOffering = null;
		Course course = null;

		if (schedule != null && teachingPreference != null) {
			teachingPreference.setSchedule(schedule);
		}

		// Set the instructor if exists
		if (teachingPreference != null && teachingPreference.getInstructor() != null) {
			Instructor instructor = instructorService.getInstructorById(teachingPreference.getInstructor().getId());
			teachingPreference.setInstructor(instructor);
		} else {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		// Set the courseOffering if exists
		if (teachingPreference != null && teachingPreference.getCourseOffering() != null) {
			courseOffering = courseOfferingService.findCourseOfferingById(teachingPreference.getCourseOffering().getId());
		}

		if (teachingPreference != null && teachingPreference.getCourse() != null) {
			course = courseService.findOneById(teachingPreference.getCourse().getId());
		}

		// Is this a teachingPreference based on a courseOffering?
		if (schedule != null && courseOffering != null) {
			teachingPreference.setSchedule(schedule);
			teachingPreference.setCourseOffering(courseOffering);
			httpResponse.setStatus(HttpStatus.OK.value());
		} 
		// Is this a teachingPreference based on a sabbatical/buyout/courseRelease?
		else if (schedule != null && (teachingPreference.getIsBuyout()
				|| teachingPreference.getIsSabbatical() || teachingPreference.getIsCourseRelease() ) ) {
			teachingPreference.setSchedule(schedule);
			httpResponse.setStatus(HttpStatus.OK.value());
		}
		// Is this a teachingPreference based on a course?
		else if (teachingPreference != null && course != null) {
			teachingPreference.setCourse(course);
		} else {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		if (teachingPreference.getCourseOffering() != null) {
			courseOffering = this.courseOfferingService.findCourseOfferingById(teachingPreference.getCourseOffering().getId());
		}

		if (courseOffering != null) {
			teachingPreference.setCourseOffering(courseOffering);
		}

		this.teachingPreferenceService.saveTeachingPreference(teachingPreference);
		
		UserLogger.log(currentUser, "Created teaching preference in schedule ID " + scheduleId + ", termCode " + teachingPreference.getTermCode());

		return new TeachingCallTeachingPreferenceView(teachingPreference);
	}

	@PreAuthorize("hasPermission(#id, 'teachingPreference', 'senateInstructor')"
			+ "or hasPermission(#id, 'teachingPreference', 'federationInstructor')"
			+ "or hasPermission(#id, 'teachingPreference', 'academicCoordinator')")
	@RequestMapping(value = "/api/teachingPreferences/{id}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public TeachingPreference updateTeachingPreference(@PathVariable Long id, @RequestBody TeachingPreference updatedTeachingPreference, HttpServletResponse httpResponse) {
		TeachingPreference originalTeachingPreference = this.teachingPreferenceService.findOneById(id);

		if (originalTeachingPreference == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		Workgroup workgroup = originalTeachingPreference.getSchedule().getWorkgroup();
		CourseOffering courseOffering = null;
		Course course = null;

		// Set the course or the courseOffering only if the preference is none of these: isSabbatical, isCourseRelease, and is Buyout
		if (updatedTeachingPreference.getIsBuyout() == false && updatedTeachingPreference.getIsSabbatical() == false && updatedTeachingPreference.getIsCourseRelease() == false) {

			// Valid Case: The preference has an existing courseOffering
			if (updatedTeachingPreference != null && updatedTeachingPreference.getCourseOffering() != null && updatedTeachingPreference.getCourseOffering().getId() != 0) {
				courseOffering = courseOfferingService.findCourseOfferingById(updatedTeachingPreference.getCourseOffering().getId());
			}
			// Valid Case: The preference has no courseOffering, but a course (Instructor is suggesting a course)
			else if (updatedTeachingPreference != null && updatedTeachingPreference.getCourse() != null && updatedTeachingPreference.getCourse().getId() != 0) {
				course = courseService.findOneById(updatedTeachingPreference.getCourse().getId());
			}
			// Invalid Case: This should never happen since a teachingPreference should have a course or a courseOffering if none of the boolean flags is set
			else {
				Exception e = new Exception("Provided teachingPreference is invalid, it should have one of these set: isSabbatical, isBuyout, isCourseRelease, course, or courseOffering.");
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}

		}

		// Only academicCoordinators can approve a teachingPreference
		if (updatedTeachingPreference.isApproved() && originalTeachingPreference.isApproved() == false) {
			boolean hasRole = false;
			for (UserRole userRole : currentUser.getCurrentUser().getUserRoles() ) {
				if (userRole.getRole().getName().equals("admin")) {
					hasRole = true;
					break;
				}
				else if (userRole.getRole().getName().equals("academicCoordinator") && userRole.getWorkgroup().getId() == workgroup.getId()) {
					hasRole = true;
					break;
				}
			}
			if (hasRole == false) {
				httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
				return null;
			}
		}

		// If teachingPreference is now approved, (and is not a buyout, sab, or release) ensure a courseOffering if it doesn't already exist
		if ( updatedTeachingPreference.isApproved() && updatedTeachingPreference.getCourseOffering() == null && updatedTeachingPreference.getIsSabbatical() == false
				&& updatedTeachingPreference.getIsBuyout() == false && updatedTeachingPreference.getIsCourseRelease() == false) {
			courseOffering = courseOfferingService.createCourseOfferingByCourseIdAndTermCodeAndScheduleId(originalTeachingPreference.getCourse().getId(), originalTeachingPreference.getTermCode(), originalTeachingPreference.getSchedule().getId());
			course = null;
		}

		if (courseOffering != null || course != null || updatedTeachingPreference.getIsBuyout()
				|| updatedTeachingPreference.getIsSabbatical() || updatedTeachingPreference.getIsCourseRelease()) {
			originalTeachingPreference.setCourseOffering(courseOffering);
			originalTeachingPreference.setCourse(course);
			originalTeachingPreference.setIsBuyout(updatedTeachingPreference.getIsBuyout());
			originalTeachingPreference.setIsSabbatical(updatedTeachingPreference.getIsSabbatical());
			originalTeachingPreference.setIsCourseRelease(updatedTeachingPreference.getIsCourseRelease());
			originalTeachingPreference.setApproved(updatedTeachingPreference.isApproved());

			httpResponse.setStatus(HttpStatus.OK.value());
			UserLogger.log(currentUser, "Updated teaching preference with ID " + id + ", termCode " + updatedTeachingPreference.getTermCode());
		} else {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
		}

		return this.teachingPreferenceService.saveTeachingPreference(originalTeachingPreference);
	}

	@PreAuthorize("hasPermission(#id, 'teachingPreference', 'senateInstructor')"
			+ "or hasPermission(#id, 'teachingPreference', 'federationInstructor')"
			+ "or hasPermission(#id, 'teachingPreference', 'academicCoordinator')")
	@RequestMapping(value = "/api/teachingPreferences/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteTeachingPreference(@PathVariable Long id, HttpServletResponse httpResponse) {
		TeachingPreference teachingPreference = this.teachingPreferenceService.findOneById(id);
		UserLogger.log(currentUser, "Deleted teaching preference with ID " + id + ", termCode " + teachingPreference.getTermCode());
		this.teachingPreferenceService.deleteTeachingPreferenceById(id);
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}

	// Accepts an array of TeachingPreferenceIds, changes their priority values to match their ordering in the list
	@PreAuthorize("hasPermission('*', 'senateInstructor') or hasPermission('*', 'federationInstructor')")
	@RequestMapping(value = { "/api/teachingPreferences" }, method = { RequestMethod.PUT })
	@ResponseBody
	public void updatePreferencesOrder(@RequestBody List<Long> sortedTeachingPreferenceIds, HttpServletResponse httpResponse) {
		Long priority = 1L;

		for(Long id : sortedTeachingPreferenceIds) {
			TeachingPreference teachingPreference = teachingPreferenceService.findOneById(id);
			String subjectCode = "";
			String courseNumber = "";

			if (teachingPreference.getCourseOffering() != null ) {
				subjectCode = teachingPreference.getCourseOffering().getSubjectCode();
				courseNumber = teachingPreference.getCourseOffering().getCourseNumber();
			} else if (teachingPreference.getCourseOffering() != null) {
				subjectCode = teachingPreference.getCourse().getSubjectCode();
				courseNumber = teachingPreference.getCourse().getCourseNumber();
			}

			if (subjectCode.length() > 0) {
				UserLogger.log(currentUser, "Requested to set teaching preference " + teachingPreference.getTermCode() + " "
						+ subjectCode + " " + courseNumber + " priority from " + teachingPreference.getPriority() + " to " + priority);
			} else {
				String description = "";
				if (teachingPreference.getIsBuyout()) {
					description = "buyout";
				} else if (teachingPreference.getIsCourseRelease()) {
					description = "course release";

				} else if (teachingPreference.getIsSabbatical()) {
					description = "sabbatical";
				}
				UserLogger.log(currentUser, "Requested to set teaching preference " + description + " priority from "
				+ teachingPreference.getPriority() + " to " + priority);
			}

			// Ensure the teachingPreferenceId corresponds to a TeachingPreference owned by the current user before modifying
			if( teachingPreference.getInstructor().getLoginId().equalsIgnoreCase( authenticationService.getCurrentUser().getLoginid() )) {
				teachingPreference.setPriority(priority);
				teachingPreferenceService.saveTeachingPreference(teachingPreference);
				UserLogger.log(currentUser, "Priority request applied");
				priority++;
			} else {
				httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
				return;
			}
		}
	}

	// Accepts an array of TeachingPreferenceIds, changes their priority values to match their ordering in the list
	@PreAuthorize("hasPermission(#scheduleId, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = { "/api/schedules/{scheduleId}/teachingPreferences" }, method = { RequestMethod.PUT })
	@ResponseBody
	public void updatePreferencesOrderByCoordinator(@RequestBody List<Long> sortedTeachingPreferenceIds, @PathVariable Long scheduleId) {
		Long priority = 1L;

		for(Long id : sortedTeachingPreferenceIds) {
			TeachingPreference teachingPreference = teachingPreferenceService.findOneById(id);

			String subjectCode = "";
			String courseNumber = "";

			if (teachingPreference.getCourseOffering() != null ) {
				subjectCode = teachingPreference.getCourseOffering().getSubjectCode();
				courseNumber = teachingPreference.getCourseOffering().getCourseNumber();
			} else if (teachingPreference.getCourseOffering() != null) {
				subjectCode = teachingPreference.getCourse().getSubjectCode();
				courseNumber = teachingPreference.getCourse().getCourseNumber();
			}

			if (subjectCode.length() > 0) {
				UserLogger.log(currentUser, "Requested to set teaching preference " + teachingPreference.getTermCode() + " "
						+ subjectCode + " " + courseNumber + " priority from " + teachingPreference.getPriority() + " to " + priority);
			} else {
				String description = "";
				if (teachingPreference.getIsBuyout()) {
					description = "buyout";
				} else if (teachingPreference.getIsCourseRelease()) {
					description = "course release";

				} else if (teachingPreference.getIsSabbatical()) {
					description = "sabbatical";
				}
				UserLogger.log(currentUser, "Requested to set teaching preference " + description + " priority from "
				+ teachingPreference.getPriority() + " to " + priority);
			}

			teachingPreference.setPriority(priority);
			teachingPreferenceService.saveTeachingPreference(teachingPreference);
			priority++;
		}
	}

	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}/teachingPreferences", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	public List<TeachingPreference> getCourseOfferingGroupTeachingPreferences(@PathVariable Long id, Model model) {
		return this.teachingPreferenceService.getTeachingPreferencesByCourseOfferingGroupId(id);
	}

	@PreAuthorize("hasPermission(#sectionId, 'section', 'academicCoordinator')")
	@RequestMapping(value = "/api/sections/{sectionId}/instructors/{instructorId}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public TeachingPreference approveTeachingPreference(@PathVariable Long sectionId, @PathVariable Long instructorId,
			@RequestParam(value = "approve", required = true) boolean approve, HttpServletResponse httpResponse) {
		TeachingPreference teachingPreference = teachingPreferenceService.findOrCreateOneBySectionIdAndInstructorId(sectionId, instructorId);
		if (teachingPreference == null) return null;

		teachingPreference.setApproved(approve);
		return teachingPreferenceService.saveTeachingPreference(teachingPreference);
	}
}
