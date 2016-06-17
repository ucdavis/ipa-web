package edu.ucdavis.dss.ipa.api.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.GraduateStudentService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.TeachingAssistantPreferenceService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.api.views.UserViews;

@RestController
public class TeachingAssistantPreferenceController {
	@Inject AuthenticationService authenticationService;
	@Inject UserService userService;
	@Inject ScheduleService scheduleService;
	@Inject GraduateStudentService graduateStudentService;
	@Inject SectionGroupService sectionGroupService;
	@Inject TeachingAssistantPreferenceService teachingAssistantPreferenceService;

	/**
	 * Loads the page for managing the 'teaching assistant assignment process'
	 * Will be responsible for assigning TAs, assigning readers, and reconciling instructorTApreferences with TApreferences
	 * http://localhost:8080/IPA/api/schedules/1/terms/201410/teachingAssistantAssignment
	 * @param scheduleId
	 * @param termCode
	 * @param model
	 * @return
	 */
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/schedules/{scheduleId}/terms/{termCode}/teachingAssistantAssignment", method = RequestMethod.GET)
	public String teachingAssistantAssignment(@PathVariable long scheduleId, @PathVariable String termCode, Model model) {
		return "teachingAssistantAssignment";
	}
	
	/**
	 * Loads the page for teaching assistants to enter their preferences
	 * @param scheduleId
	 * @param termCode
	 * @param model
	 * @return
	 */
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(value = "/schedules/{scheduleId}/terms/{termCode}/teachingAssistantPreferences", method = RequestMethod.GET)
	public String teachingAssistantPreference(@PathVariable long scheduleId, @PathVariable String termCode, Model model) {
		return "teachingAssistantPreference";
	}

	/**
	 * Used to approve an existing teachingAssistantPreference from a TA, or to create a new teachingAssistantPreference for a TA
	 * and set it to approved at the same time.
	 * In our modeling, a teachingAssistantPreference is both a way to indicate desire to TA for a sectionGroup,
	 * and a historical record of who TA'd for the sectionGroup.
	 * @param id
	 * @param httpResponse
	 * @param teachingAssistant
	 * @return
	 */
	@PreAuthorize("hasPermission(#id, 'workgroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/sectionGroups/{id}/approveTeachingAssistant", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public TeachingAssistantPreference approveTeachingAssistantForSectionGroup(@PathVariable long id, HttpServletResponse httpResponse, @RequestBody GraduateStudent teachingAssistant) {
		// NOTE: graduate students can be approved from: within the sectionGroup's workgroup, outside the workgroup but already in IPA, or from outside IPA (requiring a DW people search and import with a null workgroup)
		GraduateStudent ipaGraduateStudent = graduateStudentService.findOneByLoginId(teachingAssistant.getLoginId());
		SectionGroup sectionGroup = sectionGroupService.getSectionGroupById(id);
		
		if(ipaGraduateStudent == null) {
			teachingAssistant = graduateStudentService.saveGraduateStudent(teachingAssistant);
		}
		
		TeachingAssistantPreference teachingAssistantPreference = new TeachingAssistantPreference();
		
		teachingAssistantPreference.setApproved(true);
		teachingAssistantPreference.setSectionGroup(sectionGroup);
		teachingAssistantPreference.setGraduateStudent(ipaGraduateStudent);
		
		teachingAssistantPreferenceService.saveTeachingAssistantPreference(teachingAssistantPreference);
		
		return teachingAssistantPreference;
	}
	
	/**
	 * Used to un-approve an existing teachingPreference
	 * @param id
	 * @param taPrefid
	 * @param httpResponse
	 */
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/sectionGroups/{id}/teachingAssistantPreference/{taPrefid}/unapproveTeachingAssistant", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public void unApproveTeachingAssistantForSectionGroup(@PathVariable long id, @PathVariable long taPrefid, HttpServletResponse httpResponse) {
		TeachingAssistantPreference teachingAssistantPreference = teachingAssistantPreferenceService.findOneById(taPrefid);
		
		if(teachingAssistantPreference == null) { return; }
		
		teachingAssistantPreference.setApproved(false);

		teachingAssistantPreferenceService.saveTeachingAssistantPreference(teachingAssistantPreference);
	}
	
	/**
	 * Used to update the teachingAssistantCount property on a sectionGroup
	 * @param id
	 * @param teachingAssistantCount
	 * @param httpResponse
	 */
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/sectionGroups/{id}/updateTeachingAssistantCount", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public void updateTeachingAssistantCount(@PathVariable long id, @RequestBody Long teachingAssistantCount, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.findOneById(id);

		if(sectionGroup == null || teachingAssistantCount < 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		
		sectionGroup.setTeachingAssistantCount(teachingAssistantCount);
		
		sectionGroupService.saveSectionGroup(sectionGroup);
	}
	
	/**
	 * Used to update the readerCount property on a sectionGroup
	 * @param id
	 * @param readerCount
	 * @param httpResponse
	 */
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	@RequestMapping(value ="/api/sectionGroups/{id}/updateReaderCount", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	public void updateReaderCount(@PathVariable long id, @RequestBody Long readerCount, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.findOneById(id);

		if(sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		
		sectionGroup.setReaderCount(readerCount);
		
		sectionGroupService.saveSectionGroup(sectionGroup);
	}

	/**
	 * Accepts an ordered array of teachingAssistantPreference Ids, uses this to reset rank of preferences
	 * @param sortedTeachingPreferenceIds
	 */
	@PreAuthorize("hasPermission('*', 'senateInstructor') or hasPermission('*', 'federationInstructor')")
	@RequestMapping(value = { "/api/graduateStudents/{graduateStudentId}/teachingAssistantPreferences" }, method = { RequestMethod.PUT })
	@ResponseBody
	// SECUREME
	public void updateteachingAssistantPreferencesOrder(@RequestBody List<Long> sortedTeachingPreferenceIds) {
		Long rank = 1L;
		
		for(Long id : sortedTeachingPreferenceIds) {
			TeachingAssistantPreference teachingAssistantPreference = teachingAssistantPreferenceService.findOneById(id);

			// Is this teachingAssistantPref owned by the currently logged in user?
			if( teachingAssistantPreference.getGraduateStudent().getLoginId().equals( authenticationService.getCurrentUser().getLoginid())) {
				teachingAssistantPreference.setRank(rank);
				teachingAssistantPreferenceService.saveTeachingAssistantPreference(teachingAssistantPreference);
				rank++;
			}
		}
	}
	
	/**
	 * Used by TAs create a new teachingAssistantPreference
	 * @param sectionGroupId
	 * @param TaId
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value ="/api/sectionGroups/{sectionGroupId}/graduateStudents/{TaId}", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(UserViews.Detailed.class)
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public TeachingAssistantPreference createTeachingAssistantPreference(@PathVariable long sectionGroupId, @PathVariable long TaId, HttpServletResponse httpResponse) {
		GraduateStudent graduateStudent = graduateStudentService.findOneById(TaId);
		SectionGroup sectionGroup = sectionGroupService.getSectionGroupById(sectionGroupId);
		TeachingAssistantPreference teachingAssistantPreference = new TeachingAssistantPreference();

		if(graduateStudent == null || sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return teachingAssistantPreference;
		}
		
		teachingAssistantPreference.setApproved(false);
		teachingAssistantPreference.setSectionGroup(sectionGroup);
		teachingAssistantPreference.setGraduateStudent(graduateStudent);
		
		Integer preferenceCount = teachingAssistantPreferenceService.getTeachingAssistantPreferencesByScheduleIdAndTermCodeAndGraduateStudentId(sectionGroup.getCourseOfferingGroup().getSchedule().getId(), sectionGroup.getTermCode(), TaId).size();
		Long rank = Long.valueOf(preferenceCount + 1);
		teachingAssistantPreference.setRank(rank);
		teachingAssistantPreferenceService.saveTeachingAssistantPreference(teachingAssistantPreference);
	
		return teachingAssistantPreference;
	}
	
	/**
	 * Used by TAs to delete one of their teachingAssistantPreferences
	 * @param graduateStudentId
	 * @param teachingAssistantPreferenceId
	 * @param httpResponse
	 */
	@RequestMapping(value ="/api/graduateStudents/{graduateStudentId}/teachingAssistantPreferences/{teachingAssistantPreferenceId}", method = RequestMethod.DELETE)
	@ResponseBody
	// SECUREME
	@PreAuthorize("isAuthenticated()")
	public void deleteTeachingAssistantPreference(@PathVariable long graduateStudentId, @PathVariable long teachingAssistantPreferenceId, HttpServletResponse httpResponse) {
		teachingAssistantPreferenceService.deleteTeachingAssistantPreferenceServiceById(teachingAssistantPreferenceId);
		httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
	}
	
	/**
	 * Used by TAs to update one of their teachingAssistantPreferences
	 * @param graduateStudentId
	 * @param teachingAssistantPreferenceId
	 * @param httpResponse
	 * @param teachingAssistantPreference
	 */
	@RequestMapping(value ="/api/graduateStudents/{graduateStudentId}/teachingAssistantPreferences/{teachingAssistantPreferenceId}", method = RequestMethod.PUT)
	@ResponseBody
	@PreAuthorize("isAuthenticated()")
	// SECUREME
	public void updateTeachingAssistantPreference(@PathVariable long graduateStudentId, @PathVariable long teachingAssistantPreferenceId, HttpServletResponse httpResponse, @RequestBody TeachingAssistantPreference teachingAssistantPreference) {
		TeachingAssistantPreference ipaTeachingAssistantPreference = teachingAssistantPreferenceService.findOneById(teachingAssistantPreferenceId);
		SectionGroup sectionGroup = sectionGroupService.findOneById(teachingAssistantPreference.getSectionGroup().getId());
		
		if(ipaTeachingAssistantPreference == null || sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		ipaTeachingAssistantPreference.setSectionGroup(sectionGroup);
		ipaTeachingAssistantPreference = teachingAssistantPreferenceService.saveTeachingAssistantPreference(ipaTeachingAssistantPreference);
	}
}