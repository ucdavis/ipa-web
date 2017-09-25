package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.SectionRepository;
import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
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
	@Inject DataWarehouseRepository dataWarehouseRepository;
	@Inject ActivityService activityService;
	@Inject NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	@Inject DataWarehouseRepository dwRepository;
	@Inject JdbcTemplate jdbcTemplate;
	@Inject SectionService sectionService;
	@Inject EmailService emailService;

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

	/**
	 * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
	 */
	@Transactional
	@Override
	public void updateSectionsFromDW() {

		List<Course> courses = this.courseService.getAllCourses();

		// Map Keys will look like allDwSections.get(PSC-2017);
		Map<String, List<DwSection>> allDwSections = new HashMap<>();

		for (Course course : courses) {

			Long year = course.getSchedule().getYear();
			String subjectCode = course.getSubjectCode();
			String dwSectionKey = subjectCode + "-" + year;

			// Query the subjectCode/year pair if necessary
			if (allDwSections.get(dwSectionKey) == null) {
				List<DwSection> dwSections = dataWarehouseRepository.getSectionsBySubjectCodeAndYear(subjectCode, year);
				if (dwSections == null) {
					// If query fails to return results for the query, don't attempt to requery on a later section
					allDwSections.put(dwSectionKey, new ArrayList<DwSection>());
				}

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

							if (hasValidSequenceNumber(section)) {
								section = this.sectionRepository.save(section);
							}

						}

						activityService.syncActivityLocations(dwSection, section.getActivities());
						activityService.syncActivityLocations(dwSection, sectionGroup.getActivities());
					}
				}
			}
		}

		// Sync data from banner into empty ipa SectionGroups
		this.updateEmptySectionGroups();
	}

	private class QueryParam {
		public String subjectCode;
		public String termCode;

		QueryParam (String subjectCode, String termCode) {
			this.subjectCode = subjectCode;
			this.termCode = termCode;
		}
	}

	private void updateEmptySectionGroups() {
		// Find emptySectionGroups in IPA
		List<SectionGroup> emptySectionGroups = sectionGroupService.findEmpty();

		// Generate termCode+subjectCode pairs to query DW for to get relevant sections
		List<QueryParam> queryParams = jdbcTemplate.query("SELECT DISTINCT sg.TermCode, c.SubjectCode" +
				" FROM SectionGroups sg" +
				" LEFT JOIN Courses c ON sg.CourseId = c.Id" +
				" LEFT JOIN Sections st ON sg.Id = st.sectionGroupId" +
				" WHERE st.Id IS NULL;",
				(rs, rowNum) -> new QueryParam(
									rs.getString("SubjectCode"),
									rs.getString("TermCode")
								)
		);

		// Query DW for potentially matching sections
		List<DwSection> allDwSections = new ArrayList<>();

		for (QueryParam queryParam : queryParams) {
			List<DwSection> slotDwSections = dwRepository.getSectionsBySubjectCodeAndTermCode(queryParam.subjectCode, queryParam.termCode);
			allDwSections.addAll(slotDwSections);
		}

		// Identify which dwSections match an IPA emptySectionGroup, and persist section/activities
		for (DwSection dwSection : allDwSections) {
			// Convert dw sequenceNumber to sequencePattern
			Character dwSequenceNumberStart = dwSection.getSequenceNumber().charAt(0);
			String sequencePattern = null;

			if (Character.isLetter(dwSequenceNumberStart)) {
				sequencePattern = String.valueOf(dwSequenceNumberStart);
			} else {
				sequencePattern = dwSection.getSequenceNumber();
			}

			// Generate matchingKey for dwSection, example: '201610-ART-001-A'
			String dwMatchingKey = dwSection.getTermCode() + "-" + dwSection.getSubjectCode() + "-" + dwSection.getCourseNumber() + "-" + sequencePattern;

			// Find matching ipa sectionGroup
			for (SectionGroup sectionGroup : emptySectionGroups) {
				String ipaMatchingKey = sectionGroup.getTermCode() + "-" + sectionGroup.getCourse().getSubjectCode() + "-" + sectionGroup.getCourse().getCourseNumber() + "-" + sectionGroup.getCourse().getSequencePattern();

				if (ipaMatchingKey.equals(dwMatchingKey)) {
					// Create section
					Section section = new Section();
					section.setSectionGroup(sectionGroup);

					section.setCrn(dwSection.getCrn());
					section.setSequenceNumber(dwSection.getSequenceNumber());
					section.setSeats(dwSection.getMaximumEnrollment());

					// Using sectionRepository as this sync needs to bypass the 'term lock' validation
					section = sectionRepository.save(section);

					// Create activities
					for (DwActivity dwActivity : dwSection.getActivities()) {
						Activity activity = activityService.createFromDwActivity(dwActivity);

						Term term = termService.getOneByTermCode(sectionGroup.getTermCode());
						activity.setEndDate(term.getEndDate());
						activity.setBeginDate(term.getStartDate());
						activity.setSection(section);
						activityService.saveActivity(activity);
					}
				}
			}
		}

		// Identify activities that exist on all sections, and move them to the sectionGroup sharedActivities
		for (SectionGroup sectionGroup : emptySectionGroups) {
			sectionGroupService.identifyAndCondenseSharedActivities(sectionGroup);
		}
	}

	@Override
	public List<Section> findVisibleByWorkgroupIdAndYear(long workgroupId, long year) {
		return sectionRepository.findVisibleByWorkgroupIdAndYear(workgroupId, year);
	}

	private boolean isLocked(Section section) {
		return false;
	}

	/**
	 * Returns true if the sequenceNumber of the section is unique within the sectionGroup
	 * @param section
	 * @return
     */
	private boolean hasValidSequenceNumber (Section section) {
		String potentialSequenceNumber = section.getSequenceNumber();

		if (section.getSectionGroup() == null) {
			return false;
		}

		for (Section slotSection : section.getSectionGroup().getSections()) {
			if (slotSection.getSequenceNumber().equals(potentialSequenceNumber) && section.getId() != slotSection.getId()) {
				return false;
			}
		}

		return true;
	}
}
