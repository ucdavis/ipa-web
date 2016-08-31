package edu.ucdavis.dss.ipa.api.controllers;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.views.SectionViews;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SectionController {
	private static final Logger log = LogManager.getLogger();

	@Inject SectionService sectionService;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;
	@Inject CourseService courseService;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/sectionGroups/{id}/sections", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public Section addSection(@RequestBody Section section,
			@PathVariable Long id,
			HttpServletResponse httpResponse) {

		Section newSection = this.sectionGroupService.addSection(id, section);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.OK.value());
		} else {
			httpResponse.setStatus(HttpStatus.OK.value());
		}

		return newSection;
	}

	@PreAuthorize("hasPermission(#id, 'section', 'academicCoordinator')")
	@RequestMapping(value = "/api/sections/{id}", method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteSection(@PathVariable Long id, HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(id);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		String termCode = section.getSectionGroup().getTermCode();

		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(termCode);

		if (termState != null && termState.scheduleTermLocked()) {
			log.info("Section with ID " + id + " was not deleted because the term is locked or the section does not exist.");
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return;
		}

		SectionGroup sectionGroup = sectionGroupService.getOneById(section.getSectionGroup().getId());
		int sectionsInSectionGroup = sectionGroup.getSections().size();

		// If this was the last section, delete the sectionGroup as well
		if (sectionsInSectionGroup == 1) {
			sectionGroupService.delete(sectionGroup.getId());
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		} else if (this.sectionService.delete(id)) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}

	@PreAuthorize("hasPermission(#courseOfferingGroupId, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{courseOfferingGroupId}/sections/{sequence}", method = RequestMethod.DELETE)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public void deleteSectionSequence(@PathVariable String sequence, @PathVariable Long courseOfferingGroupId,
			HttpServletResponse httpResponse) {
		if (this.sectionService.deleteByCourseIdAndSequence(courseOfferingGroupId, sequence)) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		} else {
			log.info("Some sections with sequence " + sequence + " were not deleted because the term is locked.");
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
		}
	}

	@PreAuthorize("hasPermission(#courseOfferingGroupId, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{courseOfferingGroupId}/sections/{oldSequence}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public void updateSectionSequence(@RequestBody String newSequence, @PathVariable String oldSequence,
			@PathVariable Long courseOfferingGroupId, HttpServletResponse httpResponse) {
		if (this.sectionService.updateSequencesByCourseId(courseOfferingGroupId, oldSequence, newSequence)) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		} else {
			log.info("Some sections belong to locked term. Hence, no changes were made");
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
		}
	}

	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')")
	@RequestMapping(value = "/api/schedules/{id}/sections", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@JsonView(SectionViews.Detailed.class)
	public List<Section> getSectionsByScheduleAndTermCode(@PathVariable long id, @RequestParam(value = "termCode", required = true) String termCode,
			HttpServletResponse httpResponse) {
		List<Section> sections = new ArrayList<Section>();

		for(Course course : scheduleService.findById(id).getCourses() ) {
			for (SectionGroup sectionGroup : course.getSectionGroups()) {
				if ( sectionGroup.getTermCode().equals(termCode)) {
					sections.addAll(sectionGroup.getSections());
				}
			}
		}
		return sections;
	}
}