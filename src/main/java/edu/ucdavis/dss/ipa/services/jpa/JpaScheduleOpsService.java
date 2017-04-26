package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.ScheduleRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.ScheduleOpsService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaScheduleOpsService implements ScheduleOpsService {
	private static final Logger log = LogManager.getLogger("ScheduleOps");

	@Inject ScheduleRepository scheduleRepository;
	@Inject ScheduleService scheduleService;
	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject
	CourseService courseService;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;
	@Inject ActivityService activityService;
	@Inject UserService userService;
	@Inject InstructorService instructorService;
	@Inject RoleService roleService;
	@Inject UserRoleService userRoleService;
	@Inject WorkgroupService workgroupService;
	@Inject DataWarehouseRepository dwRepository;

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
	 * Adjusts the year in termCode, e.g. 199710 becomes 201610
	 * 
	 * @param termCode the termCode to be adjusted
	 * @param toYear the academic year to be adjusted to, e.g. 2016-17 should be "2016"
	 * @return the updated termCode
	 */
	private String adjustTermCodeYear(String termCode, Long toYear) {
		if(termCode == null) return null;
		
		String term = termCode.substring(4);
		
		// Academic year, e.g. 2016, is 2016-2017 where 201605 is the start and 201704 is the end,
		// so if the term code is less than 5, we need to increment the toYear.
		String newYear = Long.valueOf(term) < 5 ? String.valueOf(toYear + 1) : String.valueOf(toYear);
		String newTermCode = newYear + term;

		return newTermCode;
	}
}
