package edu.ucdavis.dss.ipa.web.controllers;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.web.views.SectionGroupViews;

@RestController
public class SectionGroupController {
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleService scheduleService;

	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')"
			+ "or hasPermission(#id, 'schedule', 'senateInstructor') or hasPermission(#id, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/schedules/{id}/sectionGroups", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(SectionGroupViews.Detailed.class)
	public List<SectionGroup> getSectionGroupsByScheduleAndTermCode(
			@PathVariable long id,
			@RequestParam(value = "termCode", required = false) String termCode,
			HttpServletResponse httpResponse) {
		return this.sectionGroupService.getSectionGroupsByScheduleIdAndTermCode(id, termCode);
	}

	/**
	 * Gets historical sectionGroups of the latest 3 years for a given courseId
	 * 
	 * @param id courseId
	 * @param toYear (optional) latest year, defaults to current year
	 * @return  a list sectionGroups
	 */
	@RequestMapping(value = "/api/courses/{id}/sectionGroups", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasPermission('*', 'academicCoordinator')"
			+ "or hasPermission('*', 'senateInstructor') or hasPermission('*', 'federationInstructor')")
	// SECUREME
	public List<DwSectionGroup> getSectionGroupsByCourseId (@PathVariable Long id,
			@RequestParam(value = "toYear", required = false) String toYear,
			@RequestParam(value = "term", required = false) String term
			) {
		return sectionGroupService.getSectionGroupsByCourseId(id, toYear + term);
	}
}
