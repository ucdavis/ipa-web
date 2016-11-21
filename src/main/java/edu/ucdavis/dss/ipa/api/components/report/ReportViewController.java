package edu.ucdavis.dss.ipa.api.components.report;

import edu.ucdavis.dss.ipa.api.components.report.views.SectionDiffView;
import edu.ucdavis.dss.ipa.api.components.report.views.factories.ReportViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class ReportViewController {

	@Inject ReportViewFactory reportViewFactory;
	@Inject TermService termService;
	@Inject SectionService sectionService;
	@Inject SectionGroupService sectionGroupService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject InstructorService instructorService;
	@Inject ActivityService activityService;

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

		return sectionService.save(originalSection);
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
		if (instructorToAssign == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
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

}
