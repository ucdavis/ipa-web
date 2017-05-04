package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.RestDataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JpaSectionService implements SectionService {

	@Inject SectionRepository sectionRepository;
	@Inject CourseService courseService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject TermService termService;
	@Inject SectionGroupService sectionGroupService;
	@Inject RestDataWarehouseRepository restDataWarehouseRepository;
	@Inject ActivityService activityService;

	@Override
	public Section save(@Valid Section section) {
		if (isLocked(section)) return null;

		return sectionRepository.save(section);
	}

	@Override
	@Transactional
	public boolean delete(Long id) {
		Section section = this.getOneById(id);
		if (isLocked(section)) return false;

		this.sectionRepository.delete(id);
		return true;
	}

	@Override
	public Section getOneById(Long id) {
		return this.sectionRepository.findById(id);
	}

	/**
	 * Based on a new course sequencePattern, will recalculate and save a new section sequenceNumber
	 * @param sectionId
	 * @param newSequencePattern
     * @return
     */
	@Override
	public Section updateSequenceNumber(Long sectionId, String newSequencePattern) {
		Section section = this.getOneById(sectionId);

		if (newSequencePattern == null || newSequencePattern.length() == 0 || this.isLocked(section)) {
			return null;
		}

		Character firstChar = null;
		boolean isNumeric = true;

		firstChar = newSequencePattern.charAt(0);
		if (Character.isLetter(firstChar)) {
			isNumeric = false;
		} else {
			return null;
		}

		if (isNumeric) {
			section.setSequenceNumber(newSequencePattern);
		} else {
			String newSequenceNumber = firstChar + section.getSequenceNumber().substring(1);
			section.setSequenceNumber(newSequenceNumber);
		}

		return this.save(section);
	}

	@Override
	public List<Section> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return sectionRepository.findByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public Section findOrCreateBySectionGroupIdAndSequenceNumber(long sectionGroupId, String sequenceNumber) {
		Section section = sectionRepository.findBySectionGroupIdAndSequenceNumber(sectionGroupId, sequenceNumber);
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			return null;
		}

		if (section == null) {
			section = new Section();
			section.setSequenceNumber(sequenceNumber);
			section.setSectionGroup(sectionGroup);
			section = sectionRepository.save(section);
		}

		return section;
	}

	@Transactional
	@Override
	public void updateSectionsFromDW() {

		// Update Courses to have the proper units value
		List<Course> courses = this.courseService.getAllCourses();

		// Map Keys will look like allDwSections.get(PSC-2017);
		Map<String, List<DwSection>> allDwSections = new HashMap<>();

		for (Course course : courses) {

			Long year = course.getSchedule().getYear();
			String subjectCode = course.getSubjectCode();
			String dwSectionKey = subjectCode + "-" + year;

			// Query the subjectCode/year pair if necessary
			if (allDwSections.get(dwSectionKey) == null) {
				List<DwSection> dwSections = restDataWarehouseRepository.getSectionsBySubjectCodeAndYear(subjectCode, year);

				if (dwSections == null) {
					continue;
				}

				allDwSections.put(dwSectionKey, dwSections);
			}


			// Loop through course children
			for (SectionGroup sectionGroup : course.getSectionGroups()) {
				for (Section section : sectionGroup.getSections()) {

					// Find relevant dwSections to sync from
					for (DwSection dwSection : allDwSections.get(dwSectionKey)) {
						// Ensure dwSection identification data is valid
						if (dwSection.getTermCode() == null || dwSection.getTermCode().length() == 0
								|| dwSection.getSequenceNumber() == null || dwSection.getSequenceNumber().length() == 0) {
							continue;
						}

						// Check termCode matches
						if (sectionGroup.getTermCode().equals(dwSection.getTermCode()) == false) {
							continue;
						}

						// Check sequenceNumber matches
						if (section.getSequenceNumber().equals(dwSection.getSequenceNumber()) == false) {
							continue;
						}

						// Sync crn if DW data is valid and different
						if (dwSection.getCrn() != null && dwSection.getCrn().length() > 0
								&& dwSection.getCrn().equals(section.getCrn()) == false) {
							section.setCrn(dwSection.getCrn());
							section = this.sectionRepository.save(section);
						}

						activityService.syncActivityLocations(dwSection, section.getActivities());
						activityService.syncActivityLocations(dwSection, sectionGroup.getActivities());
					}
				}
			}
		}
	}

	private boolean isLocked(Section section) {
		SectionGroup sectionGroup = section.getSectionGroup();
		if (sectionGroup == null) { return true; }

		Course course = sectionGroup.getCourse();
		Term term = termService.getOneByTermCode(sectionGroup.getTermCode());

		if (course == null) { return true; }
		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(term);

		if (termState != null && termState.scheduleTermLocked()) {
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + term.getTermCode() + " is locked")
					);
			return true;
		}

		return false;
	}
}
