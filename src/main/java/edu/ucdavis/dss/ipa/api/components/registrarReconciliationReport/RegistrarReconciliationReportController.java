package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport;

import com.fasterxml.jackson.databind.JsonNode;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffView;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.factories.ReportViewFactory;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.ActivityType;
import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.SyncAction;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.CourseService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.SyncActionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrarReconciliationReportController {
	@Inject ReportViewFactory reportViewFactory;
	@Inject CourseService courseService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject TermService termService;
	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject InstructorService instructorService;
	@Inject ActivityService activityService;
	@Inject ScheduleService scheduleService;
	@Inject SyncActionService syncActionService;
	@Inject UserService userService;
	@Inject UserRoleService userRoleService;
	@Inject WorkgroupService workgroupService;
	@Inject Authorizer authorizer;

	/**
	 * Delivers the available termStates for the initial report form.
	 *
	 * @param workgroupId
	 * @return
	 */
	@RequestMapping(value = "/api/reportView/workgroups/{workgroupId}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Term> getTermsToCompare(@PathVariable long workgroupId) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return termService.findActiveTermCodesByWorkgroupId(workgroupId);
	}

	/**
	 * Delivers the JSON payload for the Diff View.
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @return
	 */
	@RequestMapping(value = "/api/reportView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<SectionDiffView> showDiffView(@PathVariable long workgroupId, @PathVariable long year,
											  @PathVariable String termCode) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return reportViewFactory.createDiffView(workgroupId, year, termCode);
	}

	/**
	 * Updates section crn and seats
	 *
	 * @param sectionId
	 * @param section
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value = "/api/reportView/sections/{sectionId}", method = RequestMethod.PUT, produces = "application/json")
	@ResponseBody
	public Section updateSection(@PathVariable long sectionId, @RequestBody Section section, HttpServletResponse httpResponse) {
		Section originalSection = sectionService.getOneById(sectionId);
		if (originalSection == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = originalSection.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		if (section.getCrn() != null) {
			originalSection.setCrn(section.getCrn());
		}
		if (section.getSeats() != null) {
			originalSection.setSeats(section.getSeats());
		}

		originalSection = sectionService.save(originalSection);

		if (originalSection == null){
		    // Section is locked
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        } else {
		    return originalSection;
        }
	}

	@RequestMapping(value = "/api/reportView/sectionGroups/{sectionGroupId}/instructors", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public TeachingAssignment assignInstructor(@PathVariable long sectionGroupId, @RequestBody Instructor instructor, HttpServletResponse httpResponse) {

		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Instructor instructorToAssign = instructorService.getOneByLoginId(instructor.getLoginId());

		// In case the instructor is not in IPA yet
		if (instructorToAssign == null) {
			// Get the user info from DW
			User user = userService.findOrCreateByLoginId(instructor.getLoginId());
			if (user == null) {
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			// Add the "instructor" role to the new user (This will also create the instructor)
			userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(
						instructor.getLoginId(), workgroup.getId(), "instructor");

			instructorToAssign = instructorService.getOneByLoginId(instructor.getLoginId());
		}

		TeachingAssignment assignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructorToAssign);
		assignment.setApproved(true);
		return teachingAssignmentService.saveAndAddInstructorType(assignment);
	}


	@RequestMapping(value = "/api/reportView/sectionGroups/{sectionGroupId}/instructors/{loginId}", method = RequestMethod.DELETE, produces = "application/json")
	@ResponseBody
	public TeachingAssignment unAssignInstructor(@PathVariable long sectionGroupId, @PathVariable String loginId, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Instructor instructorToAssign = instructorService.getOneByLoginId(loginId);
		if (instructorToAssign == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		TeachingAssignment teachingAssignment = teachingAssignmentService.findOneBySectionGroupAndInstructorAndTermCodeAndApprovedTrue(sectionGroup, instructorToAssign, sectionGroup.getTermCode());

		// When an academicCoordinator unapproves a teachingAssignment made by an academicCoordinator, delete instead of updating
		if (!teachingAssignment.isFromInstructor()) {
			teachingAssignmentService.delete(teachingAssignment.getId());
			return null;
		}

		teachingAssignment.setApproved(false);

		return teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);
	}

	@RequestMapping(value = "/api/reportView/activities/{activityId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces="application/json")
	@ResponseBody
	public Activity updateActivity(@PathVariable long activityId, @RequestBody JsonNode node, HttpServletResponse httpResponse) {
		Activity originalActivity = activityService.findOneById(activityId);
		if (originalActivity == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
		SectionGroup sectionGroup = sectionGroupService.getOneById(originalActivity.getSectionGroupIdentification());
		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		if (node.has("dayIndicator")) {
			originalActivity.setDayIndicator(node.get("dayIndicator").textValue());
		}
		if (node.has("startTime")) {
			originalActivity.setStartTime(Utilities.convertToTime(node.get("startTime").textValue()));
		}
		if (node.has("endTime")) {
			originalActivity.setEndTime(Utilities.convertToTime(node.get("endTime").textValue()));
		}
		if (node.has("bannerLocation")) {
			originalActivity.setBannerLocation(node.get("bannerLocation").textValue());
		}

		return this.activityService.saveActivity(originalActivity);
	}

	@RequestMapping(value = "/api/reportView/activities/{activityId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteActivity(@PathVariable long activityId, HttpServletResponse httpResponse) {
		Activity activity = activityService.findOneById(activityId);

		if (activity == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		SectionGroup sectionGroup = sectionGroupService.getOneById(activity.getSectionGroupIdentification());
		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		this.activityService.deleteActivityById(activity.getId());
	}

	@RequestMapping(value = "/api/reportView/sections/{sectionId}/activities/{activityCode}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Activity createActivity(@PathVariable char activityCode,
								   @PathVariable Long sectionId,
								   @RequestBody Activity activity,
								   HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(sectionId);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = section.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		ActivityType activityType = new ActivityType(activityCode);
		activity.setActivityTypeCode(activityType);
		activity.setActivityState(ActivityState.DRAFT);
		activity.setSection(section);

		return activityService.saveActivity(activity);
	}

	@RequestMapping(value = "/api/reportView/sections/{sectionId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteSection(@PathVariable long sectionId, HttpServletResponse httpResponse) {
		Section section = sectionService.getOneById(sectionId);

		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = section.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		sectionService.delete(sectionId);
	}

	@RequestMapping(value = "/api/reportView/syncActions", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public SyncAction createSyncAction(@RequestBody SyncAction syncAction,
									   HttpServletResponse httpResponse) {
		if (syncAction == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Section section = sectionService.getOneById(syncAction.getSectionIdentification());
		SectionGroup sectionGroup = sectionGroupService.getOneById(syncAction.getSectionGroupIdentification());
		Workgroup workgroup = null;

		if (section != null) {
			workgroup = section.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		} else if (sectionGroup != null) {
			workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		return syncActionService.save(syncAction);
	}

	@RequestMapping(value = "/api/reportView/sectionGroups/{sectionGroupId}/sections/{sequenceNumber}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public SectionDiffView createSection(@PathVariable long sectionGroupId,
										@PathVariable String sequenceNumber,
										@RequestBody Section sectionDto,
										HttpServletResponse httpResponse) {

		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Section section = new Section();
		section.setSeats(sectionDto.getSeats());
		section.setSequenceNumber(sectionDto.getSequenceNumber());
		section.setCrn(sectionDto.getCrn());
		section.setSectionGroup(sectionGroup);
		section = this.sectionService.save(section);

		List<Activity> activities = new ArrayList<>();
		for (Activity activityDto : sectionDto.getActivities()) {
			// Make activities
			Activity activity = new Activity();

			activity.setActivityState(ActivityState.DRAFT);
			activity.setBannerLocation(activityDto.getBannerLocation());
			activity.setActivityTypeCode(activityDto.getActivityTypeCode());
			activity.setDayIndicator(activityDto.getDayIndicator());
			activity.setStartTime(activityDto.getStartTime());
			activity.setEndTime(activityDto.getEndTime());
			activity.setBeginDate(activityDto.getBeginDate());
			activity.setEndDate(activityDto.getEndDate());

			activity.setSection(section);
			activity = activityService.saveActivity(activity);

			boolean contains = false;

			for (Activity slotActivity : activities) {
				if (slotActivity.getId() == activity.getId()) {
					contains = true;
				}
			}

			if (contains == false) {
				activities.add(activity);
			}
		}

		section.setActivities(activities);

		// Make user, userRoles, instructor and teachingAssignment if necessary
		if (sectionDto.getSectionGroup() != null) {
			for (TeachingAssignment teachingAssignmentDto : sectionDto.getSectionGroup().getTeachingAssignments()) {
				Instructor instructorDto = teachingAssignmentDto.getInstructor();

				if (instructorDto == null || instructorDto.getLoginId() == null || instructorDto.getLoginId().length() == 0) {
					continue;
				}

				User user = userService.findOrCreateByLoginId(instructorDto.getLoginId());

				// Ensure they are an instructor
				Instructor instructor = null;
				if(user != null) {
					instructor = userRoleService.findOrAddActiveInstructor(workgroup, user);
				} else {
					instructor = instructorService.findOrCreate(instructorDto.getFirstName(), instructorDto.getLastName(), instructorDto.getEmail(), instructorDto.getLoginId(), workgroup.getId(), instructorDto.getUcdStudentSID());
				}

				// Make a teachingAssignment for that instructor
				TeachingAssignment teachingAssignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
				teachingAssignment.setApproved(true);
				teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);
			}
		}

		return reportViewFactory.createDiffView(section, sectionDto);
	}

	@RequestMapping(value = "/api/reportView/sectionGroups/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public SectionDiffView createSectionGroup(@PathVariable long workgroupId, @PathVariable long year, @PathVariable String termCode, @RequestBody Course courseDto, HttpServletResponse httpResponse) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		Course existingCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(courseDto.getSubjectCode(), courseDto.getCourseNumber(), courseDto.getSequencePattern(), schedule.getId());
		SectionGroup sectionGroup;
		Section sectionDto = courseDto.getSectionGroups().get(0).getSections().get(0);
		Section section;

		if (existingCourse != null) {
			sectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(existingCourse.getId(), termCode);

			courseDto.setSectionGroups(Arrays.asList(sectionGroup));
		} else {
			List<DwCourse> dwCourses = dwRepository.searchCourses(courseDto.getSubjectCode() + " " + courseDto.getCourseNumber());
			DwCourse dwCourse = dwCourses.get(0);

			if (!dwCourse.getSubjectCode().equals(courseDto.getSubjectCode()) && !dwCourse.getCourseNumber().equals(courseDto.getCourseNumber())) {
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			Course newCourse = new Course();
			newCourse.setSubjectCode(courseDto.getSubjectCode());
			newCourse.setCourseNumber(courseDto.getCourseNumber());
			newCourse.setTitle(dwCourse.getTitle());
			newCourse.setSequencePattern(courseDto.getSequencePattern());
			newCourse.setEffectiveTermCode(dwCourse.getEffectiveTermCode());
			newCourse.setUnitsLow(dwCourse.getCreditHoursLow());
			newCourse.setUnitsHigh(dwCourse.getCreditHoursHigh());
			newCourse.setSchedule(schedule);

			Course savedCourse = courseService.create(newCourse);

			sectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(savedCourse.getId(), termCode);

		}

		section = sectionService.findOrCreateBySectionGroupAndSequenceNumber(sectionGroup, sectionDto.getSequenceNumber());

		List<Activity> activities = new ArrayList<>();
		for (Activity activityDto : sectionDto.getActivities()) {
			// Make activities
			Activity activity = new Activity();

			activity.setActivityState(ActivityState.DRAFT);
			activity.setBannerLocation(activityDto.getBannerLocation());
			activity.setActivityTypeCode(activityDto.getActivityTypeCode());
			activity.setDayIndicator(activityDto.getDayIndicator());
			activity.setStartTime(activityDto.getStartTime());
			activity.setEndTime(activityDto.getEndTime());
			activity.setBeginDate(activityDto.getBeginDate());
			activity.setEndDate(activityDto.getEndDate());

			activity.setSection(section);
			activity = activityService.saveActivity(activity);

			boolean contains = false;

			for (Activity slotActivity : activities) {
				if (slotActivity.getId() == activity.getId()) {
					contains = true;
				}
			}

			if (contains == false) {
				activities.add(activity);
			}
		}

		section.setCrn(sectionDto.getCrn());
		section.setSeats(sectionDto.getSeats());
		section.setActivities(activities);
		section = sectionService.save(section);

		return reportViewFactory.createDiffView(section, sectionDto);
	};

	@RequestMapping(value = "/api/reportView/syncActions/{syncActionId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteSyncAction(@PathVariable long syncActionId, HttpServletResponse httpResponse) {
		SyncAction syncAction = syncActionService.getOneById(syncActionId);
		if (syncAction == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = null;

		if (syncAction.getSection() != null) {
			workgroup = syncAction.getSection().getSectionGroup().getCourse().getSchedule().getWorkgroup();
		}

		if (syncAction.getSectionGroup() != null) {
			workgroup = syncAction.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		}

		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		syncActionService.delete(syncActionId);
	}
}
