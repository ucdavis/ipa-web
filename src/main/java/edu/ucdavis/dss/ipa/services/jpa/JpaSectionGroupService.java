package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JpaSectionGroupService implements SectionGroupService {
	@Inject SectionGroupRepository sectionGroupRepository;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;
	@Inject SectionService sectionService;
	@Inject CourseService courseService;
	@Inject InstructorService instructorService;
	@Inject WorkgroupService workgroupService;

	@Override
	@Transactional
	//@PreAuthorize("hasAnyRole('admin','academicCoordinator')")
	public SectionGroup getOneById(Long id) {
		return sectionGroupRepository.findOne(id);
	}

	@Override
	public SectionGroup save(SectionGroup sectionGroup) {
		return this.sectionGroupRepository.save(sectionGroup);
	}

	@Override
	public void delete(Long id) {
		this.sectionGroupRepository.delete(id);
	}

	@Override
	public List<SectionGroup> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
		Schedule schedule = this.scheduleService.findById(scheduleId);
		List<SectionGroup> sectionGroups = schedule.getCourses()
				.stream()
				.map(course -> course.getSectionGroups().stream()
						.filter(sectionGroup -> termCode == null || termCode.trim().isEmpty() || sectionGroup.getTermCode().equals(termCode.trim()))
						.collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return sectionGroups;
	}

	/*
	 * 1- find or create a CO that has a matching COG AND termCode, then add section to it.
	 * 2- Find a sectionGroup that has a matching sequence pattern, or create a new one
	 */
	@Override
	@Transactional
	public Section addSection(Long sectionGroupId, Section section) {
		if (isLocked(sectionGroupId)) return null;

		SectionGroup sectionGroup = this.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			return null;
		} else {
			section.setSectionGroup(sectionGroup);
		}

		return sectionService.save(section);
	}

	@Override
	public List<SectionGroup> findByWorkgroupIdAndYear(long workgroupId, long year) {
		return sectionGroupRepository.findByCourseScheduleWorkgroupIdAndCourseScheduleYear(workgroupId, year);
	}

	@Override
	public List<SectionGroup> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return sectionGroupRepository.findByCourseScheduleWorkgroupIdAndCourseScheduleYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public List<SectionGroup> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		List<SectionGroup> occupiedVisibleSectionGroups = sectionGroupRepository.findOccupiedVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<SectionGroup> emptySectionGroups = sectionGroupRepository.findEmptyByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		occupiedVisibleSectionGroups.addAll(emptySectionGroups);
		Collections.sort(occupiedVisibleSectionGroups, (o1, o2) -> o1.getCourse().getShortDescription().compareTo(o2.getCourse().getShortDescription()));
		return occupiedVisibleSectionGroups;
	}

	private boolean isLocked(long sectionGroupId) {
		SectionGroup sectionGroup = this.getOneById(sectionGroupId);
		if (sectionGroup == null) { return false; }

		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(sectionGroup.getTermCode());

		if (termState != null && termState.scheduleTermLocked()) {
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + sectionGroup.getTermCode() + " is locked")
			);
			return true;
		}

		return false;
	}

}
