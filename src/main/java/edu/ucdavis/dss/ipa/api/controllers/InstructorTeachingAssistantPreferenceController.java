package edu.ucdavis.dss.ipa.api.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.GraduateStudent;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorTeachingAssistantPreference;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTeachingAssistantPreferenceService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.api.views.UserViews;

@RestController
public class InstructorTeachingAssistantPreferenceController {
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject ScheduleService scheduleService;
	@Inject GraduateStudentService graduateStudentService;
	@Inject SectionGroupService sectionGroupService;
	@Inject InstructorTeachingAssistantPreferenceService instructorTeachingAssistantPreferenceService;
	@Inject InstructorService instructorService;
	
	/**
	 * Loads the page for instructors to enter their instructor teachingAssistant preferences
	 * 
	 * @param scheduleId
	 * @param termCode
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/schedules/{scheduleId}/terms/{termCode}/instructorTeachingAssistantPreferences", method = RequestMethod.GET)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public String InstructorTeachingAssistantPreference(@PathVariable long scheduleId, @PathVariable String termCode, Model model)
	{
		return "instructorTeachingAssistantPreference";
	}

	/**
	 * Accepts an ordered array of instructorTeachingAssistantPreference Ids, uses this to reset rank of preferences
	 * 
	 * @param instructorId
	 * @param sortedInstructorTeachingPreferenceIds
	 */
	@PreAuthorize("hasPermission('*', 'senateInstructor') or hasPermission('*', 'federationInstructor')")
	@RequestMapping(value = { "/api/instructors/{instructorId}/instructorTeachingAssistantPreferences" }, method = { RequestMethod.PUT })
	@ResponseBody
	public void updateInstructorTeachingAssistantPreferencesOrder(@PathVariable Long instructorId, @RequestBody List<Long> sortedInstructorTeachingPreferenceIds)
	{
		instructorTeachingAssistantPreferenceService.sortInstructorTeachingAssistantPreferences(instructorId, sortedInstructorTeachingPreferenceIds);
	}
	
	/**
	 * Used by instructors to delete one of their instructorTeachingAssistantPreferences
	 * 
	 * @param graduateStudentId
	 * @param teachingAssistantPreferenceId
	 * @param httpResponse
	 */
	@RequestMapping(value ="/api/instructors/{instructorId}/instructorTeachingAssistantPreferences/{instructorTeachingAssistantPreferenceId}", method = RequestMethod.DELETE)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public void deleteTeachingAssistantPreference(@PathVariable long instructorId, @PathVariable long instructorTeachingAssistantPreferenceId, HttpServletResponse httpResponse) {
		instructorTeachingAssistantPreferenceService.deleteInstructorTeachingAssistantPreferenceServiceById(instructorTeachingAssistantPreferenceId);
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}
	
	/**
	 * Used by instructors to update one of their instructorTeachingAssistantPreferences
	 * @param graduateStudentId
	 * @param teachingAssistantPreferenceId
	 * @param httpResponse
	 * @param teachingAssistantPreference
	 */
	@RequestMapping(value ="/api/instructors/{instructorId}/instructorTeachingAssistantPreferences/{instructorTeachingAssistantPreferenceId}", method = RequestMethod.PUT)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public void updateInstructorTeachingAssistantPreference(@PathVariable long instructorId, @PathVariable long instructorTeachingAssistantPreferenceId, HttpServletResponse httpResponse, @RequestBody InstructorTeachingAssistantPreference instructorTeachingAssistantPreference) {
		InstructorTeachingAssistantPreference ipaInstructorTeachingAssistantPreference = instructorTeachingAssistantPreferenceService.findOneById(instructorTeachingAssistantPreferenceId);
		SectionGroup sectionGroup = sectionGroupService.findOneById(instructorTeachingAssistantPreference.getSectionGroup().getId());
		GraduateStudent teachingAssistant = graduateStudentService.findOneById(instructorTeachingAssistantPreference.getGraduateStudent().getId());
				
		if(ipaInstructorTeachingAssistantPreference == null || sectionGroup == null || teachingAssistant == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		ipaInstructorTeachingAssistantPreference.setSectionGroup(sectionGroup);
		ipaInstructorTeachingAssistantPreference.setGraduateStudent(teachingAssistant);
		ipaInstructorTeachingAssistantPreference = instructorTeachingAssistantPreferenceService.saveInstructorTeachingAssistantPreference(ipaInstructorTeachingAssistantPreference);
	}
	
	/**
	 * Returns a list of sectionGroups that have approved teachingPreferences from the instructor on the given term/schedule
	 * @param id
	 * @param termCode
	 * @param scheduleId
	 * @return
	 */
	@RequestMapping(value = "/api/instructors/{id}/sectionGroups", method = RequestMethod.GET)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public List<SectionGroup> getSectionGroupsByCourseId (@PathVariable Long id,
			@RequestParam(value = "termCode", required = true) String termCode,
			@RequestParam(value = "scheduleId", required = true) Long scheduleId
			) {

		List<SectionGroup> sectionGroups = sectionGroupService.getSectionGroupsByScheduleIdAndTermCodeAndInstructorId(scheduleId, termCode, id);
		
		return sectionGroups;
	}
	
	/**
	 * Used by instructors to create a new instructorTeachingAssistantPreferences
	 * @param sectionGroupId
	 * @param graduateStudentId
	 * @param instructorId
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value ="/api/sectionGroups/{sectionGroupId}/graduateStudents/{graduateStudentId}/instructors/{instructorId}", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public InstructorTeachingAssistantPreference createInstructorTeachingAssistantPreference(
			@PathVariable long sectionGroupId,
			@PathVariable long graduateStudentId,
			@PathVariable long instructorId,
			HttpServletResponse httpResponse) {

		GraduateStudent graduateStudent = graduateStudentService.findOneById(graduateStudentId);
		SectionGroup sectionGroup = sectionGroupService.getSectionGroupById(sectionGroupId);
		Instructor instructor = instructorService.getInstructorById(instructorId);
		InstructorTeachingAssistantPreference instructorTeachingAssistantPreference = new InstructorTeachingAssistantPreference();

		if(graduateStudent == null || sectionGroup == null || instructor == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return instructorTeachingAssistantPreference;
		}
		
		instructorTeachingAssistantPreference.setInstructor(instructor);
		instructorTeachingAssistantPreference.setSectionGroup(sectionGroup);
		instructorTeachingAssistantPreference.setGraduateStudent(graduateStudent);
		
		Integer preferenceCount = instructorTeachingAssistantPreferenceService.getInstructorTeachingAssistantPreferencesByScheduleIdAndTermCodeAndInstructorId(sectionGroup.getCourseOfferingGroup().getSchedule().getId(), sectionGroup.getTermCode(), instructorId).size();
		Long rank = Long.valueOf(preferenceCount + 1);
		instructorTeachingAssistantPreference.setRank(rank);

		instructorTeachingAssistantPreferenceService.saveInstructorTeachingAssistantPreference(instructorTeachingAssistantPreference);

		return instructorTeachingAssistantPreference;
	}
}