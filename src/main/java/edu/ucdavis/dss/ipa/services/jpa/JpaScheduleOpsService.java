package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.ScheduleRepository;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.DwScheduleService;
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
	@Inject DwScheduleService dwScheduleService;
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
//		Workgroup workgroup = workgroupService.findOneById(workgroupId);
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
//					sectionGroupService.save(newSg);
//
//					// Create the Sections
//					for(Section section : sg.getSections()) {
//						Section newSection = new Section();
//
//						newSection.setSectionGroup(newSg);
//						newSection.setSeats(section.getSeats());
//						newSection.setSequenceNumber(section.getSequenceNumber());
//						newSection.setVisible(section.isVisible());
//						newSection.setCrnRestricted(section.isCrnRestricted());
//
//						sectionService.saveSection(newSection);
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

	@Override
	public void importSchedulesFromDataWarehouse(Workgroup workgroup, long startYear, long endYear) {
		Calendar now = Calendar.getInstance();
		int currentYear = now.get(Calendar.YEAR);
		
		log.debug("importSchedulesFromDataWarehouse starting ...");
		
		for(int i = (int) startYear; i >= endYear; i--) {
			Schedule schedule = scheduleRepository.findOneByWorkgroupCodeAndYear(workgroup.getCode(), (long) i);

			// If schedule doesn't exist, create and import it
			if(schedule == null) {
				schedule = scheduleService.createSchedule(workgroup.getId(), (long) i);
				
				schedule.setImporting(true);
				
				this.scheduleRepository.save(schedule);

				log.info("Importing schedule from DW for schedule ID " + schedule.getId() + " (schedule year: " + schedule.getYear() + ", schedule workgroup code: " + schedule.getWorkgroup().getCode() + ")");
				
				Set<DwSectionGroup> dwSectionGroups = null;
				
				try {
					dwSectionGroups = dwRepository.getSectionGroupsByDeptCodeAndYear(schedule.getWorkgroup().getCode(), schedule.getYear());
					
					if(dwSectionGroups != null) {
						log.info("Received " + dwSectionGroups.size() + " section groups from DW.");
					} else {
						log.error("dwClient returned NULL section groups!");
						return; // cannot continue past this point
					}
				} catch (Exception e) {
					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
					continue;
				}
				
				boolean markPublished = true;

				if(i >= currentYear) {
					markPublished = false;
				}
				
				for(DwSectionGroup dwCo : dwSectionGroups) {
					this.dwScheduleService.addOrUpdateDwSectionGroupToSchedule(dwCo, schedule, markPublished);
				}

				schedule.setImporting(false);

				scheduleService.saveSchedule(schedule);
			}
		}
		
		log.debug("importSchedulesFromDataWarehouse finished.");
	}

	@Override
	public void importWorkgroupUsersFromDataWarehouse(Workgroup workgroup) {
		if(workgroup == null) {
			throw new IllegalStateException("Workgroup cannot be null.");
		}
		
		log.info("Importing users from DW for workgroup: " + workgroup);
		
		List<DwInstructor> dwInstructors = null;
		
		try {
			dwInstructors = dwRepository.getDepartmentInstructorsByDeptCode(workgroup.getCode());
			
			log.info("Received " + dwInstructors.size() + " users from DW.");
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}
		
		// Grab the instructorRole (we'll need this later ...)
		Role instructorRole = this.roleService.findOneByName("senateInstructor");
		if(instructorRole == null) {
			throw new IllegalStateException("IPA should have an 'instructor' role!");
		}

		for(DwInstructor dwInstructor : dwInstructors) {
			if(validDwInstructor(dwInstructor)) {
				// Find or create a user, instructor, and proper role for this DwInstructor
				
				// Create user
				User user = this.userService.findOrCreateUserByLoginId(dwInstructor.getLoginId());
				
				// Update 'user' if necessary
				if((user.getEmail() == null) || (user.getEmail().length() == 0)) {
					user.setEmail(dwInstructor.getEmailAddress());
				}
				if((user.getFirstName() == null) || (user.getFirstName().length() == 0)) {
					user.setFirstName(dwInstructor.getFirstName());
				}
				if((user.getLastName() == null) || (user.getLastName().length() == 0)) {
					user.setLastName(dwInstructor.getLastName() );
				}

				
				// Create instructor (not a role nor a user - it is a piece of data)
				Instructor instructor = this.instructorService.getOneByLoginId(dwInstructor.getLoginId());
				if(instructor == null) {
					// Some instructors may be in the DB already without a login ID ...
					instructor = this.instructorService.getOneByEmployeeId(dwInstructor.getEmployeeId());
				}

				if(instructor == null) {
					instructor = new Instructor();
				}
				
				// Set or update instructor attributes
				if((instructor.getEmail() == null) || (instructor.getEmail().length() == 0)) {
					instructor.setEmail(dwInstructor.getEmailAddress());
				}
				if((instructor.getEmployeeId() == null) || (instructor.getEmployeeId().length() == 0)) {
					instructor.setEmployeeId(dwInstructor.getEmployeeId());
				}
				if((instructor.getFirstName() == null) || (instructor.getFirstName().length() == 0)) {
					instructor.setFirstName(dwInstructor.getFirstName());
				}
				if((instructor.getLastName() == null) || (instructor.getLastName().length() == 0)) {
					instructor.setLastName(dwInstructor.getLastName());
				}
				if((instructor.getLoginId() == null) || (instructor.getLoginId().length() == 0)) {
					instructor.setLoginId(dwInstructor.getLoginId());
				}
				
				this.instructorService.save(instructor);

				// Ensure this user will have the 'instructor' role
				if(user.getRoles().contains(instructorRole) == false) {
					UserRole userRole = new UserRole();
					
					userRole.setActive(true);
					userRole.setUser(user);
					userRole.setRole(instructorRole);
					userRole.setWorkgroup(workgroup);
					
					this.userRoleService.saveUserRole(userRole);
				}
			} else {
				log.warn("Skipping an instructor during import for workgroup (" + workgroup + ") because certain fields are missing (" + dwInstructor + ").");
			}
		}
	}
	
	/**
	 * Returns true if given instructor has non-blank email, loginId, and name.
	 * 
	 * @param instructor
	 * @return
	 */
	private boolean validDwInstructor(DwInstructor instructor) {
		if(instructor.getEmailAddress() == null) return false;
		if(instructor.getEmailAddress().length() == 0) return false;
		if(instructor.getLoginId() == null) return false;
		if(instructor.getLoginId().length() == 0) return false;
		if(instructor.getFirstName() == null) return false;
		if(instructor.getFirstName().length() == 0) return false;
		if(instructor.getLastName() == null) return false;
		if(instructor.getLastName().length() == 0) return false;

		return true;
	}
}
