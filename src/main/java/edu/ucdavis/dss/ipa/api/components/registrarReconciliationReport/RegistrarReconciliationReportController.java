package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport;

import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffDto;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.SectionDiffView;
import edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views.factories.ReportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.hibernate.jdbc.Work;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class RegistrarReconciliationReportController {

	@Inject ReportViewFactory reportViewFactory;
	@Inject TermService termService;
	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject InstructorService instructorService;
	@Inject ActivityService activityService;
	@Inject SyncActionService syncActionService;
	@Inject UserService userService;
	@Inject UserRoleService userRoleService;

	/**
	 * Delivers the available termStates for the initial report form.
	 *
	 * @param workgroupId
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value = "/api/reportView/workgroups/{workgroupId}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<Term> getTermsToCompare(@PathVariable long workgroupId, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return termService.findActiveTermCodesByWorkgroupId(workgroupId);
	}

	/**
	 * Delivers the JSON payload for the Diff View.
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value = "/api/reportView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public List<SectionDiffView> showDiffView(@PathVariable long workgroupId, @PathVariable long year,
											  @PathVariable String termCode, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Instructor instructorToAssign = instructorService.getOneByLoginId(instructor.getLoginId());

		// In case the instructor is not in IPA yet
		if (instructorToAssign == null) {
			// Get the user info from DW
			User user = userService.findOrCreateByLoginId(instructor.getLoginId());
			if (user == null) {
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}

			// Add the "senateInstructor" role to the new user (This will also create the instructor)
			userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(
						instructor.getLoginId(), workgroup.getId(), "senateInstructor");

			instructorToAssign = instructorService.getOneByLoginId(instructor.getLoginId());
		}

		TeachingAssignment assignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructorToAssign);
		assignment.setApproved(true);
		return teachingAssignmentService.save(assignment);
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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Instructor instructorToAssign = instructorService.getOneByLoginId(loginId);
		if (instructorToAssign == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		TeachingAssignment teachingAssignment = teachingAssignmentService.findOneBySectionGroupAndInstructor(sectionGroup, instructorToAssign);

		// When an academicCoordinator unapproves a teachingAssignment made by an academicCoordinator, delete instead of updating
		if (!teachingAssignment.isFromInstructor()) {
			teachingAssignmentService.delete(teachingAssignment.getId());
			return null;
		}

		teachingAssignment.setApproved(false);

		return teachingAssignmentService.save(teachingAssignment);
	}

	@RequestMapping(value = "/api/reportView/activities/{activityId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Activity updateActivity(@PathVariable long activityId, @RequestBody Activity activity, HttpServletResponse httpResponse) {
		Activity originalActivity = activityService.findOneById(activityId);
		if (originalActivity == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
		SectionGroup sectionGroup = sectionGroupService.getOneById(originalActivity.getSectionGroupIdentification());
		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		if (activity.getDayIndicator() != null) {
			originalActivity.setDayIndicator(activity.getDayIndicator());
		}
		if (activity.getStartTime() != null) {
			originalActivity.setStartTime(activity.getStartTime());
		}
		if (activity.getEndTime() != null) {
			originalActivity.setEndTime(activity.getEndTime());
		}
		if (activity.getBannerLocation() != null) {
			originalActivity.setBannerLocation(activity.getBannerLocation());
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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		ActivityType activityType = new ActivityType(activityCode);
		activity.setActivityTypeCode(activityType);
		activity.setActivityState(ActivityState.DRAFT);
		activity.setSection(section);

		return activityService.saveActivity(activity);
	}

	@RequestMapping(value = "/api/reportView/sections/{sectionId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteSection(@PathVariable long sectionId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		Section section = sectionService.getOneById(sectionId);
		if (section == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		Workgroup workgroup = section.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		sectionService.delete(sectionId);
	}

	@RequestMapping(value = "/api/reportView/syncActions", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public SyncAction createSyncAction(@RequestBody SyncAction syncAction,
									   HttpServletResponse httpResponse) {
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

		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
			activity.setSection(section);
			activity.setActivityState(ActivityState.DRAFT);

			activity.setBannerLocation(activityDto.getBannerLocation());
			activity.setActivityTypeCode(activityDto.getActivityTypeCode());
			activity.setDayIndicator(activityDto.getDayIndicator());
			Character taco = activityDto.getDayIndicator().charAt(0);
			int length = activityDto.getDayIndicator().length();

			activity.setStartTime(activityDto.getStartTime());
			activity.setEndTime(activityDto.getEndTime());
			activity.setBeginDate(activityDto.getBeginDate());
			activity.setEndDate(activityDto.getEndDate());
			activity = activityService.saveActivity(activity);

			activities.add(activity);
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
				Instructor instructor = instructorService.findOrAddActiveInstructor(workgroup, user);

				// Make a teachingAssignment for that instructor
				teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
			}
		}

		return reportViewFactory.createDiffView(section, sectionDto);
	}

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

		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		syncActionService.delete(syncActionId);
	}
}
