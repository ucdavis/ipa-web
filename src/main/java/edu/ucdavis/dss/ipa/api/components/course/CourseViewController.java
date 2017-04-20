package edu.ucdavis.dss.ipa.api.components.course;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.entities.validation.CourseValidator;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Time;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class CourseViewController {
	@Inject AnnualViewFactory annualViewFactory;
	@Inject SectionGroupService sectionGroupService;
	@Inject WorkgroupService workgroupService;
	@Inject	ScheduleService scheduleService;
	@Inject TagService tagService;
	@Inject SectionService sectionService;
	@Inject CourseService courseService;
	@Inject ActivityService activityService;
	@Inject TermService termService;
	@Inject ScheduleTermStateService scheduleTermStateService;

	@Inject CourseValidator courseValidator;
	@Inject DataWarehouseRepository dwRepository;
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
	public CourseView createMultipleCourses(@RequestBody List<SectionGroupImport> sectionGroupImportList,
											@PathVariable Long workgroupId, @PathVariable Long year,
											@RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
											HttpServletResponse httpResponse) {

		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		Schedule schedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		if (sectionGroupImportList.size() == 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		String subjectCode = sectionGroupImportList.get(0).getSubjectCode();

		List<DwSection> dwSections = dwRepository.getSectionsBySubjectCodeAndYear(subjectCode, year);

		for (SectionGroupImport sectionGroupImport : sectionGroupImportList) {
			for (DwSection dwSection : dwSections) {
				String newTermCode = null;
				String shortTermCode = dwSection.getTermCode().substring(4, 6);
				if (Long.valueOf(shortTermCode) < 4) {
					newTermCode = year + shortTermCode;
				} else {
					long nextYear = year++;
					newTermCode = nextYear + shortTermCode;
				}


				Term term = termService.getOneByTermCode(sectionGroupImport.getTermCode());

				// Don't import this dwSection if termState is locked
				ScheduleTermState termState = scheduleTermStateService.createScheduleTermState(term);

				if (termState.scheduleTermLocked()) {
					continue;
				}

				// Calculate sequencePattern from sequenceNumber
				String dwSequencePattern = null;

				Character c = dwSection.getSequenceNumber().charAt(0);
				Boolean isLetter = Character.isLetter(c);
				if (isLetter) {
					dwSequencePattern = String.valueOf(c);
				} else {
					dwSequencePattern = dwSection.getSequenceNumber();
				}

				// Ensure this dwSection matches the sectionGroupImport (course) of interest
				if (sectionGroupImport.getCourseNumber().equals( dwSection.getCourseNumber() )
				&& sectionGroupImport.getSubjectCode().equals( dwSection.getSubjectCode() )
				&& sectionGroupImport.getSequencePattern().equals( dwSequencePattern )) {

					String courseNumber = sectionGroupImport.getCourseNumber();

					// Attempt to make a course
					Course course = courseService.findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
							sectionGroupImport.getSubjectCode(),
							sectionGroupImport.getCourseNumber(),
							sectionGroupImport.getSequencePattern(),
							sectionGroupImport.getTitle(),
							sectionGroupImport.getEffectiveTermCode(),
							schedule,
							true
					);

					// Attempt to make a sectionGroup
					SectionGroup sectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(course.getId(), newTermCode);
					sectionGroup.setPlannedSeats(sectionGroupImport.getPlannedSeats());
					sectionGroup = sectionGroupService.save(sectionGroup);

					// Attempt to make a section
					Section section = sectionService.findOrCreateBySectionGroupIdAndSequenceNumber(sectionGroup.getId(), dwSection.getSequenceNumber());

					section.setSeats(dwSection.getMaximumEnrollment());
					section = sectionService.save(section);

					// Make activities
					for (DwActivity dwActivity : dwSection.getActivities()) {
						Activity activity = new Activity();

						ActivityType activityType = new ActivityType();
						activityType.setActivityTypeCode(dwActivity.getSsrmeet_schd_code());

						activity.setActivityTypeCode(activityType);

						String dayIndicator = dwActivity.getDay_indicator();

						String rawStartTime = dwActivity.getSsrmeet_begin_time();
						String hours = rawStartTime.substring(0, 2);
						String minutes = rawStartTime.substring(2, 4);
						String formattedStartTime = hours + ":" + minutes + ":00";
						Time startTime = java.sql.Time.valueOf(formattedStartTime);

						activity.setStartTime(startTime);

						String rawEndTime = dwActivity.getSsrmeet_end_time();
						hours = rawStartTime.substring(0, 2);
						minutes = rawStartTime.substring(2, 4);
						String formattedEndTime = hours + ":" + minutes + ":00";
						Time endTime = java.sql.Time.valueOf(formattedStartTime);

						activity.setEndTime(endTime);

						activity.setDayIndicator(dayIndicator);

						activity.setBeginDate(term.getStartDate());
						activity.setEndDate(term.getEndDate());
						activity.setActivityState(ActivityState.DRAFT);

						activity.setSection(section);
						activityService.saveActivity(activity);
					}
				}
			}
		}

		// TODO: Look through the sectionGroups in the created course, and find any activityTypes on the sections, that should instead be a 'shared activity' on the sectionGroup

		return annualViewFactory.createCourseView(workgroupId, year, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/generateExcel", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year,
							 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
							 HttpServletRequest httpRequest) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		String url = SettingsConfiguration.getIpaApiURL() + "/download/courseView/workgroups/" + workgroupId + "/years/"+ year +"/excel";
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
