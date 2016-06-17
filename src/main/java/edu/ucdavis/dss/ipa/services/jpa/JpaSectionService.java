package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.CensusSnapshotRepository;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.services.CourseOfferingGroupService;
import edu.ucdavis.dss.ipa.services.CourseOfferingService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;

@Service
public class JpaSectionService implements SectionService {
	@Inject SectionRepository sectionRepository;
	@Inject CensusSnapshotRepository censusSnapshotRepository;

	@Inject InstructorService instructorService;
	@Inject SectionGroupService sectionGroupService;
	@Inject CourseOfferingGroupService courseOfferingGroupService;
	@Inject CourseOfferingService courseOfferingService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;

	@Override
	public Section saveSection(Section section) {
		return this.sectionRepository.save(section);
	}

	@Override
	@Transactional
	public boolean deleteSectionById(Long id) {
		Section section = this.getSectionById(id);
		if(section == null) return false;

		Course cog = section.getSectionGroup().getCourseOffering().getCourse();
		String termCode = section.getSectionGroup().getCourseOffering().getTermCode();
		if (isLocked(cog.getId(), termCode)) return false;

		// Updating parent
		SectionGroup sectionGroup = this.sectionGroupService.findOneById(section.getSectionGroup().getId());
		List<Section> sections = sectionGroup.getSections();
		sections.remove(section);
		sectionGroup.setSections(sections);
		sectionGroupService.saveSectionGroup(sectionGroup);

		this.sectionRepository.delete(id);
		return true;
	}

	@Override
	public Section getSectionById(Long id) {
		return this.sectionRepository.findById(id);
	}

	@Override
	public void saveCensusSnapshot(CensusSnapshot censusSnapshot) {
		this.censusSnapshotRepository.save(censusSnapshot);
	}

	@Override
	public Section updateSection(Section updatedSection) {
		if (updatedSection == null) return null;

		Section section = this.getSectionById(updatedSection.getId());
		if (section == null) return null;

		Course cog = section.getSectionGroup().getCourseOffering().getCourse();
		String termCode = section.getSectionGroup().getCourseOffering().getTermCode();
		if (isLocked(cog.getId(), termCode)) return null;

		return this.saveSection(updatedSection);
	}

	/*
	 * 1- find or create a CO that has a matching COG AND termCode, then add section to it.
	 * 2- Find a sectionGroup that has a matching sequence pattern, or create a new one
	 */
	@Override
	@Transactional
	public Section addSectionToCourseOfferingGroup(Long courseOfferingGroupId, String termCode, Section section) {

		Course course = this.courseOfferingGroupService.getCourseOfferingGroupById(courseOfferingGroupId);
		CourseOffering courseOffering = this.courseOfferingService.findOrCreateOneByCourseOfferingGroupAndTermCode(course, termCode);

		if (isLocked(courseOfferingGroupId, courseOffering.getTermCode())) return null;

		SectionGroup sg = this.courseOfferingGroupService.findSectionGroupByOfferingGroupIdAndTermCodeAndSequence(
				courseOfferingGroupId, termCode, section.getSequenceNumber());

		if (sg == null) {
			sg = new SectionGroup();
			sg.setCourseOffering(courseOffering);
			section.setSectionGroup(sectionGroupService.saveSectionGroup(sg));
			sg.addSection(section);
		} else {
			section.setSectionGroup(sg);
		}

		return this.saveSection(section);
	}

	@Override
	@Transactional
	public boolean deleteSectionsBySequence(
			Long courseOfferingGroupId, String sequence) {
		boolean deletedAll = true;
		Course course = this.courseOfferingGroupService.getCourseOfferingGroupById(courseOfferingGroupId);
		for (CourseOffering courseOffering : course.getSectionGroups()) {
			for (SectionGroup sg : courseOffering.getSectionGroups()) {
				List<Section> toDelete = new ArrayList<Section>();
				for (Section section : sg.getSections()) {
					if (section.getSequenceNumber().equals(sequence)) {
						toDelete.add(section);
					}
				}
				for (Section section : toDelete) {
					String termCode = section.getSectionGroup().getCourseOffering().getTermCode();
					if (isLocked(courseOfferingGroupId, termCode)) deletedAll = false;
					if (this.deleteSectionById(section.getId())) {
						sg.getSections().remove(section);
					}
				}
			}
		}
		return deletedAll;
	}

	@Override
	public boolean updateSectionSequences(Long courseOfferingGroupId, String oldSequence, String newSequence) {
		boolean renamedAll = true;
		Course course = this.courseOfferingGroupService.getCourseOfferingGroupById(courseOfferingGroupId);
		for (CourseOffering courseOffering : course.getSectionGroups()) {
			for (SectionGroup co : courseOffering.getSectionGroups()) {
				for (Section section : co.getSections()) {
					String termCode = section.getSectionGroup().getCourseOffering().getTermCode();
					if (section.getSequenceNumber().equals(oldSequence)) {
						if (isLocked(course.getId(), termCode)) renamedAll = false;
						else section.setSequenceNumber(newSequence);
					}
				}
			}
		}

		if (renamedAll) {
			this.courseOfferingGroupService.saveCourseOfferingGroup(course);
		}

		return renamedAll;
	}

	private boolean isLocked(long cogId, String termCode) {
		Course cog = this.courseOfferingGroupService.getCourseOfferingGroupById(cogId);
		Schedule schedule = cog.getSchedule();
		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(schedule, termCode);

		if (termState != null && termState.scheduleTermLocked()) {
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + termCode + " is locked in schedule with id " + schedule.getId())
					);
			return true;
		}

		return false;
	}

	@Override
	public Section getSectionByCrnAndTerm(String crn, String termCode) {
		return this.sectionRepository.findByCrnAndSectionGroupCourseOfferingTermCode(crn, termCode);
	}

	@Override
	public CensusSnapshot getCensusSnapshotBySectionIdAndSnapshotCode(long id, String snapshotCode) {
		return this.censusSnapshotRepository.findBySectionIdAndSnapshotCode(id, snapshotCode);
	}

	@Override
	public List<CensusSnapshot> getCensusSnapshotsBySectionId(long sectionId) {
		return this.censusSnapshotRepository.findBySectionId(sectionId);
	}
}
