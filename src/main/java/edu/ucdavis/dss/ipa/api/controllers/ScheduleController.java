package edu.ucdavis.dss.ipa.api.controllers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.TermState;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.DwSyncService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.Email;
import edu.ucdavis.dss.utilities.UserLogger;
import edu.ucdavis.dss.ipa.api.components.summary.views.WorkgroupScheduleView;
import edu.ucdavis.dss.ipa.api.components.summary.views.factories.SummaryViewFactory;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.TeachingCallScheduleView;
import edu.ucdavis.dss.ipa.api.components.teachingCall.views.factories.TeachingCallViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.ScheduleViews;

@RestController
public class ScheduleController {
	@Inject ScheduleService scheduleService;
	@Inject SectionService sectionService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject WorkgroupService workgroupService;
	@Inject AuthenticationService authenticationService;
	@Inject InstructorService instructorService;
	@Inject UserService userService;
	@Inject ScheduleOpsService scheduleOpsService;
	@Inject SummaryViewFactory summaryViewFactory;
	@Inject TeachingCallViewFactory teachingCallViewFactory;
	@Inject CurrentUser currentUser;
	@Inject DwSyncService dwSyncService;

	@RequestMapping(value = "/schedules", method = RequestMethod.GET, produces="text/html")
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public String getSchedules() {
		return "schedule";
	}

	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator') or hasPermission(#id, 'schedule', 'senateInstructor') or hasPermission(#id, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/schedules/{id}/summary", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public TeachingCallScheduleView getScheduleSummary(@PathVariable long id) {
		Schedule schedule = scheduleService.findById(id);
		return this.teachingCallViewFactory.createTeachingCallScheduleView(schedule);
	}

	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(ScheduleViews.Detailed.class)
	public Schedule getScheduleDetails(@PathVariable long id) {
		return this.scheduleService.findById(id);
	}
	
	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{id}/changes", method = RequestMethod.GET, produces="text/plain")
	@ResponseBody
	public String getScheduleChanges(@PathVariable long id) {
		return this.dwSyncService.jsonDifferencesFromDw(id);
	}

	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@ResponseBody
	@RequestMapping(value = "/api/schedules/blank", method = RequestMethod.POST)
	public Schedule createBlankSchedule(
			@RequestParam(value="scheduleYear", required=true) Long scheduleYear,
			Model model, HttpServletResponse response) {
		Workgroup scheduleWorkgroup = this.workgroupService.findOneById(authenticationService.getActiveWorkgroupId());
		return this.scheduleService.createSchedule(scheduleWorkgroup.getId(), scheduleYear);
	}

	/**
	 * Copies the specified years schedule data into a new schedule, copying is modified by parameters
	 * 
	 * @param scheduleYear
	 * @param model
	 * @return
	 */
	@PreAuthorize("hasPermission('*', 'academicCoordinator')")
	@ResponseBody
	@RequestMapping(value = "/api/schedules", method = RequestMethod.POST)
	public Schedule createScheduleFromExisting(@RequestParam(value="scheduleYear", required=true) Long scheduleYear,
			@RequestParam(value="copyFromYear", required=true) Long copyFromYear,
			@RequestParam(value="copyInstructors", required=true) Boolean copyInstructors,
			@RequestParam(value="copyRooms", required=true) Boolean copyRooms,
			@RequestParam(value="copyTimes", required=true) Boolean copyTimes,
			Model model, HttpServletResponse response) {
		Workgroup scheduleWorkgroup = this.workgroupService.findOneById(authenticationService.getActiveWorkgroupId());

		Schedule schedule = this.scheduleOpsService.createScheduleFromExisting(scheduleWorkgroup.getId(), scheduleYear, copyFromYear, copyInstructors, copyRooms, copyTimes);

		if(schedule != null) {
			response.setStatus(HttpStatus.OK.value());

			List<String> logArray = new ArrayList<String>();

			logArray.add("Created a new schedule for the year " + scheduleYear);
			logArray.add("copied from year " + copyFromYear);
			
			if (copyInstructors || copyRooms || copyTimes) { logArray.add("including"); }
			if (copyInstructors) { logArray.add("instructors"); }
			if (copyRooms) { logArray.add("rooms"); }
			if (copyTimes) { logArray.add("times"); }
			
			UserLogger.log(currentUser, String.join(" ", logArray));

			return schedule;
		} else {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			
			return null;
		}
	}

	// Used to share a schedule (send email, and generate share token if it doesn't already exist)
	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/schedules/{id}/share", method = RequestMethod.POST)
	public String shareSchedule(@RequestParam(value = "emails", required = true) List<String> emails, @PathVariable long id, Model model) throws MessagingException {
		Schedule schedule = scheduleService.findById(id);
		String secretToken = schedule.getSecretToken();

		if(secretToken == null) {
			SecureRandom sr = new SecureRandom();
			String token = new BigInteger(130, sr).toString(32);
			
			token = token.substring(0, Math.min(token.length(), 26));
			schedule.setSecretToken(token);

			scheduleService.saveSchedule(schedule);

			secretToken = token;
		}

		for(String email : emails) {
			String messageBody = SettingsConfiguration.getURL() + "/schedules/share/" + id + "?token=" + secretToken;
			String messageSubject = "A schedule from IPA has been shared with you";

			Email.send(email, messageBody, messageSubject);
		}

		return "schedule";
	}

	// Used when viewing a shared schedule and authenticating via secret token
	@RequestMapping(value = "/schedules/share/{id}", method = RequestMethod.GET)
	// SECUREME - needs special handling for the secret token
	@PreAuthorize("isAuthenticated()")
	public String readOnlySchedule(@RequestParam(value = "token", required = true) String token, @PathVariable long id, Model model) throws MessagingException {   
		return "schedule";
	}

	// Used when requesting rowDetails data and authenticating via secret token
	@RequestMapping(value = "/api/schedules/share/{id}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@JsonView(ScheduleViews.Detailed.class)
	// SECUREME - needs special handling for the secret token
	@PreAuthorize("isAuthenticated()")
	public Schedule observeSchedule(@RequestParam(value = "token", required = true) String token, @PathVariable long id) {
		return this.scheduleService.findById(id);
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/api/schedules/states", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String,Object>> getPossibleStates() {
		List<Map<String, Object>> possibleStates = new ArrayList<Map<String,Object>>();

		for(TermState stateEnum : TermState.values()) {
			Map<String, Object> state = new HashMap<String, Object>();
			state.put("description", stateEnum.getDescription() );
			state.put("ordinal", stateEnum.ordinal());
			possibleStates.add(state);
		}

		return possibleStates;
	}

	@PreAuthorize("hasPermission(#id, 'workgroup', 'academicCoordinator') or hasPermission(#id, 'workgroup', 'senateInstructor') or hasPermission(#id, 'workgroup', 'federationInstructor')")
	@RequestMapping(value ="/api/workgroups/{id}/schedules", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<WorkgroupScheduleView> getSchedulesByWorkgroupId(@PathVariable long id, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(id);

		return this.summaryViewFactory.createWorkgroupScheduleViews(workgroup);
	}
}
