package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.ScheduleRepository;

import java.util.*;

@Service
public class JpaScheduleOpsService implements ScheduleOpsService {
	private static final Logger log = LoggerFactory.getLogger("ScheduleOps");

	@Inject ScheduleService scheduleService;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;
	@Inject ActivityService activityService;
	@Inject InstructorService instructorService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject JdbcTemplate jdbcTemplate;
	@Inject TermService termService;

	/**
	 * Creates a new schedule by copying the specified local schedule.
	 */
	@Override
	public Schedule createScheduleFromExisting(Long workgroupId, Long newScheduleYear, Long copyFromYear, Boolean copyInstructors, Boolean copyRooms, Boolean copyTimes) {
//		Workgroup workgroup = workgroupService.getOneById(workgroupId);
//		Schedule existingSchedule = this.scheduleService.findByWorkgroupAndYear(workgroup, copyFromYear);
//
//		if (scheduleService.findByWorkgroupAndYear(workgroup, newScheduleYear) != null) {
//			log.error("Cannot createScheduleFromExisting as schedule for year: " + newScheduleYear + " already exists.");
//			return null;
//		}
//
//		if (existingSchedule == null) {
//			log.error("Cannot createScheduleFromExisting as existing schedule for year: " + copyFromYear + " does not exist.");
//			return null;
//		}
//
//		log.info("Creating schedule from existing year (" + existingSchedule.getYear() + ") to year " + newScheduleYear);
//
//		Schedule newSchedule = scheduleService.createSchedule(existingSchedule.getWorkgroup().getId(), newScheduleYear);
//
//		// Create the CourseOfferingGroups
//		for(Course cog : existingSchedule.getCourses()) {
//			Course newCog = new Course();
//
//			newCog.setSchedule(newSchedule);
//
//			List<Tag> tags = new ArrayList<Tag>();
//			for(Tag tag : cog.getTags()) {
//				tags.add(tag);
//			}
//
//			newCog.setTags(tags);
//			newCog.setCourse(cog.getCourse());
//			newCog.setTitle(cog.getTitle());
//			newCog.setUnitsLow(cog.getUnitsLow());
//			newCog.setUnitsHigh(cog.getUnitsHigh());
//
//			newCog = courseService.saveCourseOfferingGroup(newCog);
//
//			// Create the CourseOfferings
//			for(CourseOffering courseOffering : cog.getSectionGroups()) {
//				String updatedTermCode = adjustTermCodeYear(courseOffering.getTermCode(), newScheduleYear);
//
//				CourseOffering newCourseOffering = new CourseOffering();
//
//				newCourseOffering.setCourse(newCog);
//				newCourseOffering.setTermCode(updatedTermCode);
//				newCourseOffering.setSeatsTotal(courseOffering.getSeatsTotal());
//
//				courseOfferingService.saveCourseOffering(newCourseOffering);
//
//				// Create the SectionGroups
//				for(SectionGroup sg : courseOffering.getSectionGroups()) {
//					SectionGroup newSg = new SectionGroup();
//
//					newSg.setCourseOffering(newCourseOffering);
//
//					sectionGroupService.createByLoginId(newSg);
//
//					// Create the Sections
//					for(Section section : sg.getSections()) {
//						Section newSection = new Section();
//
//						newSection.setSectionGroup(newSg);
//						newSection.setPlannedSeats(section.getPlannedSeats());
//						newSection.setSequenceNumber(section.getSequenceNumber());
//						newSection.setVisible(section.isVisible());
//						newSection.setCrnRestricted(section.isCrnRestricted());
//
//						sectionService.createByLoginId(newSection);
//
//						if(copyInstructors) {
//							// Create the TeachingPreferences
//							for (Instructor instructor : section.getInstructors()) {
//								TeachingPreference teachingPreference = teachingPreferenceService.findOrCreateOneBySectionIdAndInstructorId(newSection.getId(), instructor.getId());
//								teachingPreference.setApproved(false);
//								teachingPreference.setTermCode(adjustTermCodeYear(newCourseOffering.getTermCode(), newScheduleYear));
//								teachingPreferenceService.saveTeachingPreference(teachingPreference);
//							}
//						}
//
//						// Create the activities
//						for(Activity activity : section.getActivities()) {
//							Activity newActivity = new Activity();
//
//							if(copyTimes) {
//								newActivity.setEndTime(activity.getEndTime() );
//								newActivity.setStartTime(activity.getStartTime() );
//							}
//							if(copyRooms) {
//								newActivity.setBannerLocation(activity.getBannerLocation());
//							}
//
//							newActivity.setSection(newSection);
//							newActivity.setDayIndicator(activity.getDayIndicator());
//							newActivity.setFrequency(activity.getFrequency());
//							newActivity.setVirtual(activity.isVirtual());
//							newActivity.setActivityState(ActivityState.DRAFT);
//							newActivity.setActivityTypeCode(activity.getActivityTypeCode());
//
//							activityService.saveActivity(newActivity);
//						}
//					}
//				}
//			}
//		}
//
//		return newSchedule;
		return null;
	}

	/**
	 * Syncs CRN and location data from DW to IPA, assuming the section/activities already exist
	 */
	@Override
	@Transactional
	public void updateSectionsByCourseFromDW(Course course) {
		String ipaSubjectCode = course.getSubjectCode();
		String ipaCourseNumber = course.getCourseNumber();

		List<SectionGroup> sectionGroups = this.sectionGroupService.findByCourse(course);

		for (SectionGroup sectionGroup : sectionGroups) {
			String ipaTermCode = sectionGroup.getTermCode();

			for (Section section : sectionGroup.getSections()) {
				String ipaSequenceNumber = section.getSequenceNumber();

				String uniqueKey = ipaSubjectCode + "-" + ipaCourseNumber + "-" + ipaSequenceNumber;

				List<DwSection> _allDwSections = this.dwRepository.getSectionsByTermCodeAndUniqueKeys(ipaTermCode, Arrays.asList(uniqueKey));

				DwSection dwSection = _allDwSections.stream().filter(s ->
						ipaTermCode.equals(s.getTermCode())
								&& ipaSequenceNumber.equals(s.getSequenceNumber())
								&& ipaSubjectCode.equals(s.getSubjectCode())
								&& ipaCourseNumber.equals(s.getCourseNumber())
								&& (s.getCrn() != null)
				).findFirst().orElse(null);

				if(dwSection != null) {
					// Set CRN if necessary
					if (dwSection.getCrn().equals(section.getCrn()) == false) {
						section.setCrn(dwSection.getCrn());
						section = this.sectionService.save(section);
					}

					activityService.syncActivityLocations(dwSection, section.getActivities());
					activityService.syncActivityLocations(dwSection, sectionGroup.getActivities());
				}
			}
		}
	}

	@Transactional
	@Override
	public void updateEmptySectionGroups() {
		// Find emptySectionGroups in IPA
		List<SectionGroup> emptySectionGroups = sectionGroupService.findEmpty();

		// Generate termCode+subjectCode pairs to query DW for to get relevant sections
		List<SubjectTermCode> subjectTermCodes = jdbcTemplate.query("SELECT DISTINCT sg.TermCode, c.SubjectCode" +
						" FROM SectionGroups sg" +
						" LEFT JOIN Courses c ON sg.CourseId = c.Id" +
						" LEFT JOIN Sections st ON sg.Id = st.sectionGroupId" +
						" WHERE st.Id IS NULL;",
				(rs, rowNum) -> new SubjectTermCode(
						rs.getString("SubjectCode"),
						rs.getString("TermCode")
				)
		);

		// Query DW for potentially matching sections
		List<DwSection> allDwSections = new ArrayList<>();

		for (SubjectTermCode subjectTermCode : subjectTermCodes) {
			List<DwSection> slotDwSections = dwRepository.getSectionsBySubjectCodeAndTermCode(subjectTermCode.subjectCode, subjectTermCode.termCode);
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
					section = sectionService.save(section);

					// Create activities
					for (DwActivity dwActivity : dwSection.getActivities()) {
						Activity activity = activityService.createFromDwActivity(dwActivity);

						Term term = termService.getOneByTermCode(sectionGroup.getTermCode());

						activity.setEndDate(term.getEndDate());
						activity.setBeginDate(term.getStartDate());
						activity.setSection(section);

						try {
							activityService.saveActivity(activity);
						} catch (javax.validation.ConstraintViolationException e) {
							log.error("Could not save activity based on DW activity:" + dwActivity);
						}
					}
				}
			}
		}

		// Identify activities that exist on all sections, and move them to the sectionGroup sharedActivities
		for (SectionGroup sectionGroup : emptySectionGroups) {
			sectionGroupService.identifyAndCondenseSharedActivities(sectionGroup);
		}
	}

	private class SubjectTermCode {
		public String subjectCode;
		public String termCode;

		SubjectTermCode(String subjectCode, String termCode) {
			this.subjectCode = subjectCode;
			this.termCode = termCode;
		}
	}
}
