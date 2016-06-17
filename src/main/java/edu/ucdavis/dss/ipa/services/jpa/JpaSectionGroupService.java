package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;

@Service
public class JpaSectionGroupService implements SectionGroupService {
	@Inject SectionGroupRepository sectionGroupRepository;
	@Inject SectionRepository sectionRepository;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;
	@Inject CourseService courseService;
	@Inject InstructorService instructorService;

	@Override
	@Transactional
	//@PreAuthorize("hasAnyRole('admin','academicCoordinator')")
	public SectionGroup getSectionGroupById(Long id) {
		return sectionGroupRepository.findOne(id);
	}

	@Override
	@Transactional
	//@PreAuthorize("hasAnyRole('admin','academicCoordinator')")
	public List<SectionGroup> getSectionGroups() {
		return (List<SectionGroup>)sectionGroupRepository.findAll();
	}

	@Override
	public SectionGroup saveSectionGroup(SectionGroup sectionGroup) {
		return this.sectionGroupRepository.save(sectionGroup);
	}

	@Override
	public void deleteSectionGroupById(Long id) {
		this.sectionGroupRepository.delete(id);
	}

	@Override
	@Transactional
	public SectionGroup createSectionGroup(SectionGroup co) {
		return this.createSectionGroup(co, true);
	}

	/**
	 * Takes a SectionGroup and a boolean
	 * to whether initialize a section or not
	 */
	@Override
	@Transactional
	public SectionGroup createSectionGroup(
			SectionGroup co, boolean initSection) {

		co = this.sectionGroupRepository.save(co);

		if ((co.getSections() == null || co.getSections().size() == 0) && initSection) {
			co.setSections(new ArrayList<Section>());

			Section section = new Section();
			section.setSequenceNumber("001");
			section.setSectionGroup(co);

			this.sectionRepository.save(section);

			co.addSection(section);
		}

		return this.saveSectionGroup(co);
	}

	@Override
	@Transactional
	public Section addAutoIncrementSection(long sectionGroupId) {
		Section section = new Section();
		SectionGroup co = this.getSectionGroupById(sectionGroupId);
		section.setSectionGroup(co);

		// Sort the sections in the CO by the sequence number alphabetically
		List<Section> sections = co.getSections();
		Collections.sort(sections, new Comparator<Section>() {
			@Override
			public int compare(Section s1, Section s2) {
				return s1.getSequenceNumber().compareTo(s2.getSequenceNumber());
			}
		} );

		String lastSq = sections.get(sections.size() - 1).getSequenceNumber();

		// Get the last 2 characters and increment them
		String nextSqNum = "";
		int sqLength = 1;
		if (lastSq.length() > 1 && StringUtils.isNumeric(lastSq.substring(lastSq.length() - 2))) {
			// If the last 2 characters are numbers
			sqLength = 2;
			nextSqNum = String.format("%02d", Integer.parseInt(lastSq.substring(lastSq.length() - 2)) + 1);
		} else if (lastSq.length() > 0 && StringUtils.isNumeric(lastSq.substring(lastSq.length() - 1))) {
			// If the last character is a number
			nextSqNum = String.format("%01d", Integer.parseInt(lastSq.substring(lastSq.length() - 1)) + 1);
		} else if (lastSq.length() > 0) {
			// If the last character is a letter
			nextSqNum = String.valueOf((char) (lastSq.charAt(lastSq.length() - 1) + 1));
		}

		// Concatenate the calculated sequence, save the section and return
		section.setSequenceNumber(lastSq.substring(0, lastSq.length() - sqLength) + nextSqNum);
		return this.sectionRepository.save(section);
	}

	@Override
	public String getSectionGroupSequence(long sectionGroupId) {
		List<String> sequenceSamples = this.sectionGroupRepository.findSequenceSamplesBySectionGroupId(sectionGroupId);
		
		if((sequenceSamples == null) || (sequenceSamples.size() == 0)) return null;
		
		String sequenceSample = sequenceSamples.get(0);
		
		char sequenceStartChar = sequenceSample.charAt(0);
		
		if(Character.isLetter(sequenceStartChar)) {
			return "" + sequenceStartChar;
		} else {
			return sequenceSample;
		}
	}

	@Override
	public List<SectionGroup> getSectionGroupsByScheduleIdAndTermCode(long scheduleId, String termCode) {
		Schedule schedule = this.scheduleService.findById(scheduleId);
		List<Long> courseOfferingIds = schedule.getCourses()
				.stream()
				.map(s -> s.getSectionGroups().stream()
						.filter(co -> termCode == null || termCode.trim().isEmpty() || co.getTermCode().equals(termCode.trim()))
						.map(CourseOffering::getId).collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return this.sectionGroupRepository.findByCourseOfferingIdIn(courseOfferingIds);
	}

	@Override
	public List<DwSectionGroup> getSectionGroupsByCourseId(Long courseId, String termCode) {
		Course course = courseService.findOneById(courseId);
		if (course == null) return null;

		DwClient dwClient = null;

		try {
			dwClient = new DwClient();
			List<DwSectionGroup> sectionGroups = dwClient.getCourseCensusBySubjectCodeAndCourseNumberAndEffectiveTermAndTermCode(
					course.getSubjectCode(), course.getCourseNumber(), course.getEffectiveTermCode(), termCode);

			return sectionGroups;
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public SectionGroup findOneById(Long id) {
		return sectionGroupRepository.findById(id);
	}

	/**
	 * Returns a List of sectionGroups in a specified schedule term, that have approved teachingPreferences for a specified instructor.
	 */
	@Override
	public List<SectionGroup> getSectionGroupsByScheduleIdAndTermCodeAndInstructorId(long scheduleId, String termCode, Long instructorId) {
		List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();
		Schedule schedule = this.scheduleService.findById(scheduleId);
		Instructor instructor = this.instructorService.getInstructorById(instructorId);

		for(Course course : schedule.getCourses() ) {
			for (CourseOffering courseOffering : course.getSectionGroups() ) {
				for (SectionGroup sectionGroup : courseOffering.getSectionGroups() ) {

					if (sectionGroup.getTermCode().equals(termCode) ) {
						for (TeachingPreference teachingPreference : sectionGroup.getCourseOffering().getTeachingPreferences() ) {

							if (teachingPreference.isApproved() && teachingPreference.getInstructor().equals(instructor) ) {
								sectionGroups.add(sectionGroup);
							}
						}
					}
				}
			}
		}
		return sectionGroups;
	}

	@Override
	public List<SectionGroup> getSectionGroupsByCourseOfferingId(long courseOfferingId) {
		return this.sectionGroupRepository.findByCourseOfferingId(courseOfferingId);
	}

	@Override
	@Transactional
	public List<SectionGroup> findAllEager() {
		List<SectionGroup> sectionGroups = (List<SectionGroup>) this.sectionGroupRepository.findAll();
		for (SectionGroup sectionGroup : sectionGroups) {
			Hibernate.initialize(sectionGroup.getSections());
			for (Section section : sectionGroup.getSections()) {
				Hibernate.initialize(section.getActivities());
			}
		}
		return sectionGroups;
	}

}
