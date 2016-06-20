package edu.ucdavis.dss.ipa.api.components.term;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import edu.ucdavis.dss.ipa.entities.Activity;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.TeachingAssignment;
import edu.ucdavis.dss.ipa.services.ActivityService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.SectionService;
import edu.ucdavis.dss.ipa.services.TeachingAssignmentService;
import edu.ucdavis.dss.ipa.api.components.term.views.TermActivityView;
import edu.ucdavis.dss.ipa.api.components.term.views.TermSectionGroupView;
import edu.ucdavis.dss.ipa.api.components.term.views.TermSectionView;
import edu.ucdavis.dss.ipa.api.components.term.views.TermTeachingAssignmentView;

@RestController
public class TermViewController {

	@Inject ActivityService activityService;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;
	@Inject InstructorService instructorService;
	@Inject TeachingAssignmentService teachingAssignmentService;

	// Creates relevant copies of the activity within sections of the sectionGroup
	@RequestMapping(value = "/api/sectionGroups/{id}/activities", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	public TermActivityView createSharedActivity(@RequestBody Activity activity, @PathVariable Long id, HttpServletResponse httpResponse) {
		httpResponse.setStatus(HttpStatus.OK.value());
		SectionGroup sectionGroup = sectionGroupService.getOneById(id);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Activity slotActivity = new Activity();

		for (Section section : sectionGroup.getSections() ) {
			slotActivity.setBeginDate(activity.getBeginDate());
			slotActivity.setEndDate(activity.getEndDate());
			slotActivity.setBannerLocation(activity.getBannerLocation());
			slotActivity.setStartTime(activity.getStartTime());
			slotActivity.setEndTime(activity.getEndTime());
			slotActivity.setDayIndicator(activity.getDayIndicator());
			slotActivity.setFrequency(activity.getFrequency());
			slotActivity.setActivityTypeCode(activity.getActivityTypeCode());
			slotActivity.setActivityState(activity.getActivityState());
			slotActivity.setVirtual(activity.isVirtual());
			slotActivity.setShared(true);
			slotActivity.setSection(section);

			slotActivity = activityService.saveActivity(slotActivity);

			if (slotActivity.getId() == 0) {
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
		}

		return new TermActivityView(slotActivity);
	}

	// If activity.isShared is false,  all activities that match that type but are NOT shared will be deleted (scoped to the sectionGroup)
	@RequestMapping(value = "/api/sectionGroups/{sectionGroupId}/activities/{activityId}", method = RequestMethod.DELETE)
	@ResponseBody
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	public TermSectionView deleteSharedActivities(@PathVariable Long sectionGroupId,
			@PathVariable Long activityId, HttpServletResponse httpResponse) {

		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		Activity activity = activityService.findOneById(activityId);
		if (sectionGroup == null || activity == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		if (activity.isShared()) {
			for (Section section : sectionGroup.getSections()) {
				for (int i = section.getActivities().size() -1; i >= 0; i--) {
					Activity slotActivity = section.getActivities().get(i);
					activity.isDuplicate(slotActivity);
					activityService.deleteActivityById(slotActivity.getId());
				}
			}
		} else {
			for (Section section : sectionGroup.getSections()) {
				for (int i = section.getActivities().size() -1; i >= 0; i--) {
					Activity slotActivity = section.getActivities().get(i);
					if (slotActivity.isShared() == false && slotActivity.getActivityTypeCode().getActivityTypeCode() == activity.getActivityTypeCode().getActivityTypeCode()) {
						activityService.deleteActivityById(slotActivity.getId());
						break;
					}
				}
			}
		}

		return null;
	}

	@RequestMapping(value = "/api/termView/sectionGroups/{id}/sections", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasPermission(#id, 'sectionGroup', 'academicCoordinator')")
	public TermSectionView createSection(@RequestBody Section section, @PathVariable Long id, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(id);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Section previousSection = null;

		// Find the previous section
		for (Section slotSection : sectionGroup.getSections()) {
			if (previousSection == null) {
				previousSection = slotSection;
			}
			// compareTo returns a 1 if str1 is greater than str2
			if (slotSection.getSequenceNumber().compareTo(previousSection.getSequenceNumber()) == 1) {
				previousSection = slotSection;
			}
		}

		section.setSectionGroup(sectionGroup);
		Section newSection = sectionService.save(section);

		// Cloning activities from previous section
		if (previousSection != null) {
			for (Activity slotActivity : previousSection.getActivities()) {
				Activity newActivity = new Activity();
				newActivity.setActivityTypeCode(slotActivity.getActivityTypeCode());
				newActivity.setSection(newSection);
				newActivity.setVirtual(slotActivity.isVirtual());
				newActivity.setDayIndicator(slotActivity.getDayIndicator());
				newActivity.setShared(slotActivity.isShared());
				newActivity.setActivityState(slotActivity.getActivityState());
				newActivity.setBeginDate(slotActivity.getBeginDate());
				newActivity.setEndDate(slotActivity.getEndDate());
				newActivity.setStartTime(slotActivity.getStartTime());
				newActivity.setEndTime(slotActivity.getEndTime());

				newActivity = activityService.saveActivity(newActivity);

				List<Activity> activities = newSection.getActivities();
				activities.add(newActivity);
				newSection.setActivities(activities);

				sectionService.save(newSection);
			}
		}

		httpResponse.setStatus(HttpStatus.OK.value());
		return new TermSectionView(newSection);
	}

	@PreAuthorize("hasPermission(#sectionGroupId, 'sectionGroup', 'academicCoordinator')")
	@RequestMapping(value = "/api/sectionGroups/{sectionGroupId}/instructors/{instructorId}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public TermTeachingAssignmentView createInstructorAssignment(
			@PathVariable long sectionGroupId,
			@PathVariable long instructorId,
			HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = this.sectionGroupService.getOneById(sectionGroupId);
		Instructor instructor = this.instructorService.getOneById(instructorId);
		if (sectionGroup != null && instructor != null) {
			TeachingAssignment teachingAssignment = teachingAssignmentService
					.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
			return new TermTeachingAssignmentView(teachingAssignment);
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
	}
}
