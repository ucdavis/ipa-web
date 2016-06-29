package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.*;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

@Service
public class JpaDwScheduleService implements DwScheduleService {
	private static final Logger log = LogManager.getLogger("DwImportLogger");

	@Inject ScheduleService scheduleService;
	@Inject InstructorService instructorService;
	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject UserService userService;
	@Inject RoleService roleService;
	@Inject UserRoleService userRoleService;
	@Inject CourseService courseService;
	@Inject WorkgroupService workgroupService;
	@Inject ActivityService activityService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject DataWarehouseRepository dataWarehouseRepository;

	@Override
	@Transactional
	public void addOrUpdateDwSectionGroupToSchedule(DwSectionGroup dwSg, Schedule schedule, boolean markPublished) {
//		if(schedule == null) {
//			throw new IllegalStateException("Cannot pass a NULL schedule.");
//		}
//		if(dwSg.getSections().size() == 0) {
//			throw new IllegalStateException("DW should not be returning 0 sections!");
//		}
//
//		Workgroup workgroup = schedule.getWorkgroup();
//
//		Course course = courseService.findOrCreateByEffectiveTermAndSubjectCodeAndCourseNumberAndTitle(
//				dwSg.getEffectiveTermCode(), dwSg.getSubject().getCode(), dwSg.getCourseNumber(), dwSg.getTitle());
//
//		Course cog = this.courseService.findOrCreateCourseOfferingGroupByCourseAndScheduleId(schedule.getId(), course);
//
//		cog.setUnitsHigh(dwSg.getCreditHoursHigh());
//		cog.setUnitsLow(dwSg.getCreditHoursLow());
//
//		CourseOffering co = this.courseOfferingService.findOrCreateOneByCourseOfferingGroupAndTermCode(cog, dwSg.getTermCode());
//
//		SectionGroup sectionGroup = null;
//		List<SectionGroup> sgs = this.sectionGroupService.getSectionGroupsByCourseOfferingId(co.getId());
//
//		if(sgs != null) {
//			for(SectionGroup sg : sgs) {
//				if(dwSg.getSequencePattern().equals(this.sectionGroupService.getSectionGroupSequence(sg.getId()))) {
//					sectionGroup = sg;
//					break;
//				}
//			}
//		}
//
//		if(sectionGroup == null) {
//			// Could not find SectionGroup, so create it.
//			sectionGroup = new SectionGroup();
//
//			sectionGroup.setCourseOffering(co);
//			sectionGroup = this.sectionGroupService.createByLoginId(sectionGroup);
//
//			List<SectionGroup> updatedSectionGroups = co.getSectionGroups();
//			updatedSectionGroups.add(sectionGroup);
//			co.setSectionGroups(updatedSectionGroups);
//		}
//
//		Long seatsTotal = (co.getSeatsTotal() != null) ? co.getSeatsTotal() : 0;
//
//		for(DwSection dwSection : dwSg.getSections()) {
//			Section section = this.sectionService.getSectionByCrnAndTerm(dwSection.getCrn(), dwSg.getTermCode());
//
//			if(section == null) {
//				section = new Section();
//
//				section.setSectionGroup(sectionGroup);
//				section.setCrn(dwSection.getCrn());
//				section.setSequenceNumber(dwSection.getSequenceNumber());
//				section.setSeats(dwSection.getMaximumEnrollment());
//				section.setVisible(dwSection.isVisible());
//				section.setCrnRestricted(dwSection.isCrnRestricted());
//
//				seatsTotal += dwSection.getMaximumEnrollment();
//
//				section = this.sectionService.createByLoginId(section);
//
//				List<Section> updatedSections = sectionGroup.getSections();
//				updatedSections.add(section);
//				sectionGroup.setSections(updatedSections);
//			}
//
//			sectionGroup.addSection(section);
//			sectionGroup = sectionGroupService.createByLoginId(sectionGroup);
//
//			List<SectionGroup> updatedSectionGroups = co.getSectionGroups();
//			updatedSectionGroups.add(sectionGroup);
//			co.setSectionGroups(updatedSectionGroups);
//
//			// Parse instructors
//			for(DwInstructor dwInstructor : dwSection.getInstructors()) {
//				Instructor instructor = this.instructorService.getOneByUcdStudentSID(dwInstructor.getUcdStudentSID());
//
//				if (instructor == null) {
//					instructor = new Instructor();
//
//					instructor.setEmail(dwInstructor.getEmailAddress());
//					instructor.setUcdStudentSID(dwInstructor.getUcdStudentSID());
//					instructor.setFirstName(dwInstructor.getFirstName());
//					instructor.setLastName(dwInstructor.getLastName());
//					instructor.setLoginId(dwInstructor.getLoginId());
//
//					this.instructorService.createByLoginId(instructor);
//				}
//
//				// Create a teaching assignment if it does not exist already
//				this.teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
//
//				// Associate the instructor to the workgroup
//				if(this.instructorWorkgroupRelationshipService.existsByWorkgroupIdAndInstructorId(workgroup.getId(), instructor.getId()) == false) {
//					this.instructorWorkgroupRelationshipService.createOneByWorkgroupAndInstructor(workgroup, instructor);
//				}
//			}
//
//			// Parse census snapshots
//			List<CensusSnapshot> existingCensusSnapshots = this.sectionService.getCensusSnapshotsBySectionId(section.getId());
//
//			for(DwCensusSnapshot dwCensusSnapshot : dwSection.getCensusSnapshots()) {
//				boolean snapshot_already_imported = false;
//
//				for(CensusSnapshot snapshot : existingCensusSnapshots) {
//					if(snapshot.getSnapshotCode().equals(dwCensusSnapshot.getSnapshotCode())) {
//						snapshot_already_imported = true;
//						break;
//					}
//				}
//
//				if(snapshot_already_imported == false) {
//					CensusSnapshot censusSnapshot = new CensusSnapshot();
//
//					censusSnapshot.setCurrentAvailableSeatCount((int) dwCensusSnapshot.getCurrentAvailableSeatCount());
//					censusSnapshot.setCurrentEnrollmentCount((int) dwCensusSnapshot.getCurrentEnrollmentCount());
//					censusSnapshot.setMaxEnrollmentCount((int) dwCensusSnapshot.getMaxEnrollmentCount());
//					censusSnapshot.setSection(section);
//					censusSnapshot.setSnapshotCode(dwCensusSnapshot.getSnapshotCode());
//					censusSnapshot.setStudentCount((int) dwCensusSnapshot.getStudentCount());
//					censusSnapshot.setWaitCapacityCount((int) dwCensusSnapshot.getWaitCapacityCount());
//					censusSnapshot.setWaitCount((int) dwCensusSnapshot.getWaitCount());
//
//					this.sectionService.saveCensusSnapshot(censusSnapshot);
//				}
//			}
//
//			// As activities have no known unique IDs, we're forced to simply
//			// delete all activities and re-import, as we have no way of knowing
//			// whether an activity is new, changed, etc.
//			this.activityService.deleteAllBySectionId(section.getId());
//
//			// Parse section meetings (activities)
//			for(DwMeeting dwMeeting : dwSection.getMeetings()) {
//				Activity activity = new Activity();
//				ActivityType activityType = new ActivityType();
//				activityType.setActivityTypeCode((char)dwMeeting.getScheduleCode().getScheduleCode());
//
//				activity.setSection(section);
//				activity.setBannerLocation(dwMeeting.getBuildingCode() + " " + dwMeeting.getRoomCode());
//				activity.setActivityTypeCode(activityType);
//				activity.setStartTime(dwMeeting.getBeginTime());
//				activity.setEndTime(dwMeeting.getEndTime());
//				activity.setBeginDate(dwMeeting.getStartDate());
//				activity.setEndDate(dwMeeting.getEndDate());
//				activity.setDayIndicator(dwMeeting.getDayIndicator());
//				activity.setActivityState(markPublished ? ActivityState.CONFIRMED : ActivityState.DRAFT);
//
//				// Set activity to 'Virtual' if it has no startTime nor location
//				if (dwMeeting.getBeginTime() == null && dwMeeting.getBuildingCode() == null) {
//					activity.setVirtual(true);
//				}
//
//				// Calculate activity frequency
//				long diff = dwMeeting.getEndDate().getTime() - dwMeeting.getStartDate().getTime();
//				double diffWeeks = Math.floor(diff / (7 * 24 * 60 * 60 * 1000));
//				int timesInWeek = dwMeeting.getDaysIndicated();
//
//				if (dwMeeting.getTotalMeetings() > 0) {
//					activity.setFrequency((int) Math.ceil(diffWeeks * timesInWeek / dwMeeting.getTotalMeetings()));
//				} else {
//					activity.setFrequency(0);
//				}
//
//				this.activityService.saveActivity(activity);
//
//				List<Activity> updatedActivities = section.getActivities();
//				updatedActivities.add(activity);
//				section.setActivities(updatedActivities);
//			}
//		}
//
//		co.setSeatsTotal(seatsTotal);
//		co = this.courseOfferingService.saveCourseOffering(co);
//		this.updateSharedActivities(co);
	}

	@Override
	public List<DwSectionGroup> getSectionGroupsByCourseIdAndTermCode(long courseId, String termCode) {
		Course course = this.courseService.getOneById(courseId);

		if (course == null) { return null; }

		return dataWarehouseRepository.getSectionGroupsBySubjectCodeAndCourseNumberAndEffectiveTermCodeAndTermCode(
				course.getSubjectCode(), course.getCourseNumber(), course.getEffectiveTermCode(), termCode
		);
	}

//	@Transactional
//	private void updateSharedActivities (CourseOffering courseOffering) {
//		for (SectionGroup sectionGroup : courseOffering.getSectionGroups()) {
//			this.updateSharedActivitiesInSectionGroup(sectionGroup);
//		}
//	}

//	@Transactional
//	private void updateSharedActivitiesInSectionGroup (SectionGroup sectionGroup) {
//		List<Activity> potentialSharedActivities = new ArrayList<Activity>();
//		boolean isFirstSectionInSectionGroup = true;
//
//		if (sectionGroup.getSections().size() > 1) {
//			// Determine which activities show up in every section
//			for (Section section : sectionGroup.getSections()) {
//				// Use activities from first section as a starting reference
//				if (isFirstSectionInSectionGroup) {
//					for (Activity activity : section.getActivities()) {
//						potentialSharedActivities.add(activity);
//					}
//					isFirstSectionInSectionGroup = false;
//				} else {
//					// Verify each potentialSharedActivity is present in this section, remove it if not
//					for (int i = potentialSharedActivities.size()-1; i >= 0; i--) {
//						Activity potentialSharedActivity = potentialSharedActivities.get(i);
//						boolean foundInSection = false;
//
//						for (Activity activity : section.getActivities()) {
//							if (activity.isDuplicate(potentialSharedActivity)) {
//								foundInSection = true;
//							}
//						}
//
//						if (foundInSection == false) {
//							potentialSharedActivities.remove(i);
//						}
//					}
//				}
//			}
//
//			// Set shared to true of relevant activities
//			for (Activity sharedActivity : potentialSharedActivities) {
//				for (Section section : sectionGroup.getSections()) {
//					for (Activity activity : section.getActivities()) {
//						if (activity.isDuplicate(sharedActivity) || activity.getId() == sharedActivity.getId()) {
//							activity.setShared(true);
//							activityService.saveActivity(activity);
//						}
//					}
//				}
//			}
//		}
//	}
}
