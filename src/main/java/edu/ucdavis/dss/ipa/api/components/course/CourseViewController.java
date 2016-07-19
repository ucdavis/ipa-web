package edu.ucdavis.dss.ipa.api.components.course;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.utilities.UserLogger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;

import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class CourseViewController {
	@Inject AnnualViewFactory annualViewFactory;
	@Inject SectionGroupService sectionGroupService;
	@Inject CourseService courseService;

	/**
	 * Delivers the JSON payload for the Courses View (nee Annual View), used on page load.
	 *
	 * @param workgroupId
	 * @param year
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public CourseView showCourseView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		return annualViewFactory.createCourseView(workgroupId, year);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public SectionGroup createSectionGroup(@RequestBody SectionGroup sectionGroup, HttpServletResponse httpResponse) {
		if (sectionGroup.getCourse() == null) return null;

		// TODO: Consider how we can improve the authorizer
		Course course = courseService.getOneById(sectionGroup.getCourse().getId());
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		return sectionGroupService.save(sectionGroup);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public SectionGroup updateSectionGroup(@PathVariable long sectionGroupId, @RequestBody SectionGroup sectionGroup, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		SectionGroup originalSectionGroup = sectionGroupService.getOneById(sectionGroupId);
		Workgroup workgroup = originalSectionGroup.getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		originalSectionGroup.setPlannedSeats(sectionGroup.getPlannedSeats());

		return sectionGroupService.save(sectionGroup);
	}
}
