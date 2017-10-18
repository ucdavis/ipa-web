package edu.ucdavis.dss.ipa.api.components.course;

import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.JpaAnnualViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.validation.CourseValidator;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class CourseViewController {
	@Inject AnnualViewFactory annualViewFactory;
	@Inject SectionGroupService sectionGroupService;
	@Inject	ScheduleService scheduleService;
	@Inject TagService tagService;
	@Inject SectionService sectionService;
	@Inject CourseService courseService;

	@Value("${ipa.url.api}")
	String ipaUrlApi;

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
	public CourseView showCourseView(@PathVariable long workgroupId, @PathVariable long year,
									 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
									 HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return annualViewFactory.createCourseView(workgroupId, year, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}/sections", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Section> getSectionGroupSections(@PathVariable long sectionGroupId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");

		return sectionGroup.getSections();
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
		originalSectionGroup.setShowTheStaff(sectionGroup.getShowTheStaff());

		return sectionGroupService.save(originalSectionGroup);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteSectionGroup(@PathVariable long sectionGroupId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		SectionGroup originalSectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (originalSectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = originalSectionGroup.getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		sectionGroupService.delete(sectionGroupId);
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteCourse(@PathVariable long courseId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		Course course = courseService.getOneById(courseId);

		if (course == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = course.getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		courseService.delete(courseId);
	}

	@RequestMapping(value = "/api/courseView/schedules/{workgroupId}/{year}/courses", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public List<Long> deleteMultipleCourses(@PathVariable long workgroupId, @PathVariable long year, @RequestBody List<Long> courseIds, HttpServletResponse httpResponse) {
		Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}

		Workgroup workgroup = schedule.getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		courseService.deleteMultiple(courseIds);

		return courseIds;
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Course updateCourse(@PathVariable long courseId, @RequestBody @Validated Course courseDTO, HttpServletResponse httpResponse) {
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		return courseService.update(courseDTO);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/courses", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Course createCourse(@RequestBody @Validated Course course, @PathVariable Long workgroupId, @PathVariable Long year, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		course.setSchedule(schedule);
		Course newCourse = courseService.create(course);

		if (newCourse != null) {
			return newCourse;
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}/tags/{tagId}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Course addTagToCourse(@PathVariable long courseId, @PathVariable long tagId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Tag tag = tagService.getOneById(tagId);
		if (tag == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return courseService.addTag(course, tag);
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}/tags/{tagId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public Course removeTagFromCourse(@PathVariable long courseId, @PathVariable long tagId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Tag tag = tagService.getOneById(tagId);
		if (tag == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return courseService.removeTag(course, tag);
	}

	@RequestMapping(value = "/api/courseView/sections/{sectionId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Section updateSection(@PathVariable long sectionId, @RequestBody Section section, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		Section originalSection = sectionService.getOneById(sectionId);
		if (originalSection == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = originalSection.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		originalSection.setSeats(section.getSeats());

		return sectionService.save(originalSection);
	}

	@RequestMapping(value = "/api/courseView/sections/{sectionId}", method = RequestMethod.DELETE, produces="application/json")
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

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}/sections", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Section createSection(@RequestBody Section section, @PathVariable Long sectionGroupId, HttpServletResponse httpResponse) {
		// TODO: Consider how we can improve the authorizer
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		Authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Section newSection = new Section();
		newSection.setSectionGroup(sectionGroup);
		newSection.setSequenceNumber(section.getSequenceNumber());
		newSection.setSeats(section.getSeats());

		return sectionService.save(newSection);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/sectionGroups", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public CourseView createMultipleCoursesFromDW(@RequestBody List<SectionGroupImport> sectionGroupImportList,
												  @PathVariable Long workgroupId, @PathVariable Long year,
												  @RequestParam Boolean importTimes, @RequestParam Boolean importAssignments,
												  @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
											HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		if (sectionGroupImportList.size() == 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Schedule schedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		if (this.scheduleService.createMultipleCoursesFromDw(schedule, sectionGroupImportList, importTimes, importAssignments) == false) {
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return  null;
		}

		return annualViewFactory.createCourseView(workgroupId, year, showDoNotPrint);
	}

	/**
	 * Will only import data into brand new courses.
	 * If a course already exists, but has different sectionGroup data, the course in IPA will not be modified in any way.
	 *
	 * @param sectionGroupImportList
	 * @param workgroupId
	 * @param destinationYear
	 * @param importTimes
	 * @param importAssignments
	 * @param showDoNotPrint
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{destinationYear}/createCourses", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public CourseView createMultipleCoursesFromIPA(@RequestBody List<SectionGroupImport> sectionGroupImportList,
												  @PathVariable Long workgroupId, @PathVariable Long destinationYear,
												   @RequestParam Boolean importTimes, @RequestParam Boolean importAssignments,
												  @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
												  HttpServletResponse httpResponse) {

		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		if (sectionGroupImportList.size() == 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Schedule schedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, destinationYear);

		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		if( this.scheduleService.createMultipleCoursesFromIPA(schedule, sectionGroupImportList, importTimes, importAssignments) == false) {
			httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return  null;
		}

		return annualViewFactory.createCourseView(workgroupId, destinationYear, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/queryCourses", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<JpaAnnualViewFactory.HistoricalCourse> queryCourses(
									@PathVariable long workgroupId,
									@PathVariable long year,
									@RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
									HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return annualViewFactory.createCourseQueryView(workgroupId, year, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/generateExcel", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year,
							 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
							 HttpServletRequest httpRequest) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		String url = ipaUrlApi + "/download/courseView/workgroups/" + workgroupId + "/years/"+ year +"/excel";
		String salt = RandomStringUtils.randomAlphanumeric(16).toUpperCase();

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}

		String showDoNotPrintParam = showDoNotPrint != null ? "?showDoNotPrint=" + showDoNotPrint : "";

		Map<String, String> map = new HashMap<>();
		map.put("redirect", url + "/" + salt + "/" + UrlEncryptor.encrypt(salt, ipAddress) + showDoNotPrintParam);
		return map;
	}

	/**
	 * Exports a schedule as an Excel .xls file
	 *
	 * @param workgroupId
	 * @param year
	 * @param salt
	 * @param encrypted
	 * @param showDoNotPrint
	 * @param httpRequest
	 * @return
	 * @throws ParseException
	 */
	@RequestMapping(value = "/download/courseView/workgroups/{workgroupId}/years/{year}/excel/{salt}/{encrypted}")
	public View downloadExcel(@PathVariable long workgroupId, @PathVariable long year,
							  @PathVariable String salt, @PathVariable String encrypted,
							  @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
							  HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws ParseException {
		long TIMEOUT = 30L; // In seconds

		String ipAddress = httpRequest.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = httpRequest.getRemoteAddr();
		}

		boolean isValidUrl = UrlEncryptor.validate(salt, encrypted, ipAddress, TIMEOUT);


		if (isValidUrl) {
			return annualViewFactory.createAnnualScheduleExcelView(workgroupId, year, showDoNotPrint);
		} else {
			httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
			return null;
		}
	}

}
