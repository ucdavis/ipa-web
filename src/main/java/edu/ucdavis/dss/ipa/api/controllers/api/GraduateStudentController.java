package edu.ucdavis.dss.ipa.api.controllers.api;

import java.util.List;

import javax.inject.Inject;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.GraduateStudent;
import edu.ucdavis.dss.ipa.entities.TeachingAssistantPreference;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TeachingAssistantPreferenceService;
import edu.ucdavis.dss.ipa.api.views.TeachingPreferenceViews;

@RestController
public class GraduateStudentController {
	@Inject TeachingAssistantPreferenceService teachingAssistantPreferenceService;
	@Inject GraduateStudentService graduateStudentService;
	@Inject AuthenticationService authenticationService;
	@Inject ScheduleService scheduleService;
	@Inject SectionGroupService sectionGroupService;
	
	@RequestMapping(value = "/api/graduateStudents/{id}", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public GraduateStudent graduateStudentById(@PathVariable Long id) {
		return graduateStudentService.findOneById(id);
	}

	@RequestMapping(value = "/api/graduateStudents/{graduateStudentId}/teachingAssistantPreferences", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingPreferenceViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<TeachingAssistantPreference> getTeachingAssistantPreferencesByScheduleIdAndTermCodeAndGraduateStudentId(@PathVariable long graduateStudentId,
			@RequestParam(value = "termCode", required = true) String termCode, @RequestParam(value = "scheduleId", required = true) Long scheduleId)
	{
		return teachingAssistantPreferenceService.getTeachingAssistantPreferencesByScheduleIdAndTermCodeAndGraduateStudentId(scheduleId, termCode, graduateStudentId);
	}
}