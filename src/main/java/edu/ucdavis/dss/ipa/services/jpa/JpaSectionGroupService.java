package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;

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
	public List<SectionGroup> findByWorkgroupIdAndYear(long id, long year) {
		Workgroup workgroup = workgroupService.findOneById(id);
		Schedule schedule = this.scheduleService.findByWorkgroupAndYear(workgroup, year);
		List<Course> courses = schedule.getCourses();
		List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();

		for(Course course : courses) {
			for (SectionGroup sectionGroup : course.getSectionGroups()) {
				sectionGroups.add(sectionGroup);
			}
		}

		return sectionGroups;
	}

	private boolean isLocked(long sectionGroupId) {
		SectionGroup sectionGroup = this.getOneById(sectionGroupId);
		if (sectionGroup == null) { return false; }

		Schedule schedule = sectionGroup.getCourse().getSchedule();
		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(schedule, sectionGroup.getTermCode());

		if (termState != null && termState.scheduleTermLocked()) {
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + sectionGroup.getTermCode() + " is locked in schedule with id " + schedule.getId())
			);
			return true;
		}

		return false;
	}

}
