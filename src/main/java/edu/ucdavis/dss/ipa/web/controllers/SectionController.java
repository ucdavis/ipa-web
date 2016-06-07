package edu.ucdavis.dss.ipa.web.controllers;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.web.components.term.views.TermSectionView;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.CourseOffering;
import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingPreferenceService;
import edu.ucdavis.dss.ipa.web.views.SectionViews;

@RestController
public class SectionController {
	private static final Logger log = LogManager.getLogger();

	@Inject SectionService sectionService;
	@Inject InstructorService instructorService;
	@Inject ScheduleService scheduleService;
	@Inject TeachingPreferenceService teachingPreferenceService;
	@Inject CourseService courseService;
	@Inject SectionGroupService sectionGroupService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@PreAuthorize("hasPermission(#id, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{id}/sections", method = RequestMethod.POST)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public Section addSection(@RequestBody Section section,
			@PathVariable Long id,
			@RequestParam(value = "termCode", required = true) String termCode,
			HttpServletResponse httpResponse) {

		Section newSection = this.sectionService.addSectionToCourseOfferingGroup(id, termCode,section);

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
		Section section = sectionService.getSectionById(id);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Schedule schedule = section.getSectionGroup().getCourseOffering().getCourseOfferingGroup().getSchedule();
		String termCode = section.getSectionGroup().getCourseOffering().getTermCode();

		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(schedule, termCode);

		if (termState != null && termState.scheduleTermLocked()) {
			log.info("Section with ID " + id + " was not deleted because the term is locked or the section does not exist.");
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
			return;
		}

		SectionGroup sectionGroup = sectionGroupService.findOneById(section.getSectionGroup().getId());
		int sectionsInSectionGroup = sectionGroup.getSections().size();

		// If this was the last section, delete the sectionGroup as well
		if (sectionsInSectionGroup == 1) {
			sectionGroupService.deleteSectionGroupById(sectionGroup.getId());
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		} else if (this.sectionService.deleteSectionById(id)) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		}
	}

	@PreAuthorize("hasPermission(#courseOfferingGroupId, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{courseOfferingGroupId}/sections/{sequence}", method = RequestMethod.DELETE)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public void deleteSectionSequence(@PathVariable String sequence, @PathVariable Long courseOfferingGroupId,
			HttpServletResponse httpResponse) {
		if (this.sectionService.deleteSectionsBySequence(courseOfferingGroupId, sequence)) {
			httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
		} else {
			log.info("Some sections with sequence " + sequence + " were not deleted because the term is locked.");
			httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
		}
	}

	@PreAuthorize("hasPermission(#id, 'section', 'academicCoordinator')")
	@RequestMapping(value = { "/api/sections/{id}" }, method = { RequestMethod.PUT })
	@ResponseBody
	public TermSectionView updateSection(@RequestBody Section section, @PathVariable("id") long id,
										 HttpServletResponse httpResponse_p) {
		Section originalSection = sectionService.getSectionById(id);

		originalSection.setSeats(section.getSeats());
		originalSection.setCrn(section.getCrn());
		originalSection.setSequenceNumber(section.getSequenceNumber());

		Section savedSection = this.sectionService.updateSection(originalSection);
		if (savedSection != null) {
			httpResponse_p.setStatus(HttpStatus.OK.value());
		}
		else {
			httpResponse_p.setStatus(HttpStatus.FORBIDDEN.value());
		}

		return new TermSectionView(savedSection);
	}

	@PreAuthorize("hasPermission(#courseOfferingGroupId, 'courseOfferingGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/courseOfferingGroups/{courseOfferingGroupId}/sections/{oldSequence}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(SectionViews.Summary.class)
	public void updateSectionSequence(@RequestBody String newSequence, @PathVariable String oldSequence,
			@PathVariable Long courseOfferingGroupId, HttpServletResponse httpResponse) {
		if (this.sectionService.updateSectionSequences(courseOfferingGroupId, oldSequence, newSequence)) {
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

		for(CourseOfferingGroup courseOfferingGroup : scheduleService.findById(id).getCourseOfferingGroups() ) {
			for (CourseOffering courseOffering : courseOfferingGroup.getCourseOfferings()) {
				for (SectionGroup sectionGroup : courseOffering.getSectionGroups()) {
					if ( courseOffering.getTermCode().equals(termCode)) {
						sections.addAll(sectionGroup.getSections());
					}
				}
			}
		}
		return sections;
	}
}