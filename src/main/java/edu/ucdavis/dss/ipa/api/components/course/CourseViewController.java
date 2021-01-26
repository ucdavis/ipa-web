package edu.ucdavis.dss.ipa.api.components.course;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.course.views.CourseView;
import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.JpaAnnualViewFactory;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.repositories.BudgetScenarioRepository;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.UrlEncryptor;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Array;
import java.sql.Time;
import java.text.ParseException;
import java.util.*;

@RestController
public class CourseViewController {
	private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.dss.ipa.api.components.course.CourseViewController");

	@Inject AnnualViewFactory annualViewFactory;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionGroupCostService sectionGroupCostService;
	@Inject	ScheduleService scheduleService;
	@Inject TagService tagService;
	@Inject SectionService sectionService;
	@Inject	BudgetScenarioRepository budgetScenarioRepository;
	@Inject CourseService courseService;
	@Inject ActivityService activityService;
	@Inject TermService termService;
	@Inject TeachingAssignmentService teachingAssignmentService;
	@Inject InstructorService instructorService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject Authorizer authorizer;

	@Value("${IPA_URL_API}")
	String ipaUrlApi;

	/**
	 * Delivers the JSON payload for the Courses View (nee Annual View), used on page load.
	 *
	 * @param workgroupId
	 * @param year
     * @return
     */
	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public CourseView showCourseView(@PathVariable long workgroupId, @PathVariable long year,
									 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return annualViewFactory.createCourseView(workgroupId, year, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}/sections", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Section> getSectionGroupSections(@PathVariable long sectionGroupId, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");

		return sectionGroup.getSections();
	}

	@RequestMapping(value = "/api/courseView/sectionGroups", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public SectionGroup createOrUpdateSectionGroup(@RequestBody SectionGroup sectionGroup, HttpServletResponse httpResponse) {
		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}

		Course course = courseService.getOneById(sectionGroup.getCourse().getId());

		if (course == null) {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}

		Workgroup workgroup = course.getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		SectionGroup newSectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(course.getId(), sectionGroup.getTermCode());
		newSectionGroup.setPlannedSeats(sectionGroup.getPlannedSeats());

		// Force create first section on sectionGroup creation
		String sequenceNumber = null;
		Character firstChar = course.getSequencePattern().charAt(0);

		if (Character.isLetter(firstChar)) {
			sequenceNumber = firstChar + "01";
		} else {
			sequenceNumber = course.getSequencePattern();
		}

		sectionService.findOrCreateBySectionGroupAndSequenceNumber(newSectionGroup, sequenceNumber);

		return sectionGroupService.save(newSectionGroup);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public SectionGroup updateSectionGroup(@PathVariable long sectionGroupId, @RequestBody SectionGroup sectionGroup, HttpServletResponse httpResponse) {
		SectionGroup originalSectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (originalSectionGroup == null) {
			httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
			return null;
		}

		Workgroup workgroup = originalSectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		originalSectionGroup.setPlannedSeats(sectionGroup.getPlannedSeats());
		originalSectionGroup.setShowTheStaff(sectionGroup.getShowTheStaff());

		originalSectionGroup.setTeachingAssistantAppointments(sectionGroup.getTeachingAssistantAppointments());
		originalSectionGroup.setReaderAppointments(sectionGroup.getReaderAppointments());

		originalSectionGroup.setUnitsVariable(sectionGroup.getUnitsVariable());

		if (!originalSectionGroup.getTermCode().equals(sectionGroup.getTermCode())) {
			// need to update live data sgc termCode here or it'll get deleted on budgetView load
			// update sectionGroupCosts if they exist for current year's scenarios (live data, initial request, etc...)
			List<BudgetScenario> budgetScenarios = budgetScenarioRepository.findbyWorkgroupIdAndYear(workgroup.getId(), originalSectionGroup.getCourse().getYear());

			for (BudgetScenario budgetScenario : budgetScenarios) {
				if (budgetScenario.getIsBudgetRequest()) { continue; }

				SectionGroupCost sectionGroupCost = sectionGroupCostService
					.findBySubjectCodeAndCourseNumberAndSequencePatternAndBudgetScenarioIdAndTermCode(
						originalSectionGroup.getCourse().getSubjectCode(),
						originalSectionGroup.getCourse().getCourseNumber(),
						originalSectionGroup.getCourse().getSequencePattern(),
						budgetScenario.getId(), originalSectionGroup.getTermCode());

				if (sectionGroupCost != null) {
					sectionGroupCost.setTermCode(sectionGroup.getTermCode());
				}
			}

			originalSectionGroup.setTermCode(sectionGroup.getTermCode());
		}

		return sectionGroupService.save(originalSectionGroup);
	}

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteSectionGroup(@PathVariable long sectionGroupId, HttpServletResponse httpResponse) {
		SectionGroup originalSectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (originalSectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = originalSectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		sectionGroupService.delete(sectionGroupId);
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public void deleteCourse(@PathVariable long courseId, HttpServletResponse httpResponse) {
		Course course = courseService.getOneById(courseId);

		if (course == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}

		Workgroup workgroup = course.getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		courseService.deleteMultiple(courseIds);

		return courseIds;
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public Course updateCourse(@PathVariable long courseId, @RequestBody @Validated Course courseDTO) {
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		courseDTO.setSchedule(course.getSchedule());
		return courseService.update(courseDTO);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/courses", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Course createCourse(@RequestBody @Validated Course course, @PathVariable Long workgroupId, @PathVariable Long year, HttpServletResponse httpResponse) {
		authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

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
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Tag tag = tagService.getOneById(tagId);
		if (tag == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return courseService.addTag(course, tag);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/massAddTags", method = RequestMethod.PUT, produces="application/json")
	@ResponseBody
	public void massAddTagsToCourses(@PathVariable long workgroupId, @PathVariable long year, @RequestBody MassAssignTagsDTO massAssignTags, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		if (schedule == null || massAssignTags.getCourseIds().size() == 0 || (massAssignTags.getTagIdsToAdd().size() == 0 && massAssignTags.getTagIdsToRemove().size() == 0)) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
		}

		Workgroup workgroup = schedule.getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		courseService.massAddTagsToCourses(massAssignTags.getTagIdsToAdd(), massAssignTags.getTagIdsToRemove(), massAssignTags.getCourseIds());
	}

	@JsonDeserialize(using = MassAssignTagsDTODeserializer.class)
	public class MassAssignTagsDTO {
		private List<Long> courseIds;
		private List<Long> tagIdsToAdd;
		private List<Long> tagIdsToRemove;

		public List<Long> getCourseIds() {
			return courseIds;
		}

		public void setCourseIds(List<Long> courseIds) {
			this.courseIds = courseIds;
		}

		public List<Long> getTagIdsToAdd() {
			return tagIdsToAdd;
		}

		public void setTagIdsToAdd(List<Long> tagIdsToAdd) {
			this.tagIdsToAdd = tagIdsToAdd;
		}

		public List<Long> getTagIdsToRemove() {
			return tagIdsToRemove;
		}

		public void setTagIdsToRemove(List<Long> tagIdsToRemove) {
			this.tagIdsToRemove = tagIdsToRemove;
		}
	}

	public class MassAssignTagsDTODeserializer extends JsonDeserializer<Object> {
		@Override
		public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
				throws IOException {

			ObjectCodec oc = jsonParser.getCodec();
			JsonNode node = oc.readTree(jsonParser);

			MassAssignTagsDTO massAssignTagsDTO = new MassAssignTagsDTO();
			List<Long> courseIds = new ArrayList<>();
			List<Long> tagIdsToAdd = new ArrayList<>();
			List<Long> tagIdsToRemove = new ArrayList<>();

			JsonNode arrNode = node.get("courseIds");

			if (arrNode.isArray()) {
				for (final JsonNode objNode : arrNode) {
					Long courseId = objNode.longValue();
					courseIds.add(courseId);
				}
			}

			arrNode = node.get("tagIdsToAdd");

			if (arrNode.isArray()) {
				for (final JsonNode objNode : arrNode) {
					Long tagId = objNode.longValue();
					tagIdsToAdd.add(tagId);
				}
			}

			arrNode = node.get("tagIdsToRemove");

			if (arrNode.isArray()) {
				for (final JsonNode objNode : arrNode) {
					Long tagId = objNode.longValue();
					tagIdsToRemove.add(tagId);
				}
			}

			massAssignTagsDTO.setTagIdsToAdd(tagIdsToAdd);
			massAssignTagsDTO.setTagIdsToRemove(tagIdsToRemove);
			massAssignTagsDTO.setCourseIds(courseIds);

			return massAssignTagsDTO;
		}
	}

	@RequestMapping(value = "/api/courseView/courses/{courseId}/tags/{tagId}", method = RequestMethod.DELETE, produces="application/json")
	@ResponseBody
	public Course removeTagFromCourse(@PathVariable long courseId, @PathVariable long tagId, HttpServletResponse httpResponse) {
		Course course = courseService.getOneById(courseId);
		Workgroup workgroup = course.getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

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
		Section originalSection = sectionService.getOneById(sectionId);

		if (originalSection == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Workgroup workgroup = originalSection.getSectionGroup().getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		originalSection.setSeats(section.getSeats());

		return sectionService.save(originalSection);
	}

	@RequestMapping(value = "/api/courseView/sections/{sectionId}", method = RequestMethod.DELETE, produces="application/json")
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

	@RequestMapping(value = "/api/courseView/sectionGroups/{sectionGroupId}/sections", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public Section createSection(@RequestBody Section section, @PathVariable Long sectionGroupId, HttpServletResponse httpResponse) {
		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		if (sectionGroup == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		List<Section> sections = sectionGroup.getSections();
		if (sections.stream().filter(sect -> sect.getSequenceNumber().equals(section.getSequenceNumber())).findFirst().isPresent()) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		};

		Workgroup workgroup = sectionGroup.getCourse().getSchedule().getWorkgroup();
		authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

		Section newSection = new Section();
		newSection.setSectionGroup(sectionGroup);
		newSection.setSequenceNumber(section.getSequenceNumber());
		newSection.setSeats(section.getSeats());

		return sectionService.save(newSection);
	}

	/**
	 * Assumes all courses are of the same subject code.
	 *
	 * @param sectionGroupImportList
	 * @param workgroupId
	 * @param year
	 * @param importTimes
	 * @param importAssignments
	 * @param showDoNotPrint
	 * @param httpResponse
	 * @return
	 */
	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/sectionGroups", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public CourseView createMultipleCoursesFromDW(@RequestBody List<SectionGroupImport> sectionGroupImportList,
												  @PathVariable Long workgroupId, @PathVariable Long year,
												  @RequestParam Boolean importTimes, @RequestParam Boolean importAssignments,
												  @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
											HttpServletResponse httpResponse) {
		authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		if (sectionGroupImportList.size() == 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		Schedule schedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);
		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		String subjectCode = sectionGroupImportList.get(0).getSubjectCode();

		// Cache termService lookups
		HashMap<String, Term> termHashMap = new HashMap<>();

		// Calculate academicYear from the termCode of the first sectionGroupImport
		String termCode = sectionGroupImportList.get(0).getTermCode();
		Long yearToImportFrom = termService.getAcademicYearFromTermCode(termCode);

		List<DwSection> dwSections = dwRepository.getSectionsBySubjectCodeAndYear(subjectCode, yearToImportFrom);

		// Build 'dwSectionGroups', a list of section groups with more than one section
		// Mapped to a key (example : 'ECS030A')
		Map<String, List<DwSection>> dwSharedSectionGroups = new HashMap<String, List<DwSection>>();

		for (DwSection dwSection : dwSections) {
			// Skip over numeric sections
			Character sequenceFirstChar = dwSection.getSequenceNumber().charAt(0);
			if (Character.isLetter(sequenceFirstChar) == false) { continue; }

			List<DwSection> sections = dwSharedSectionGroups.get(dwSection.getSectionGroupSortingKey());
			if(sections == null) { sections = new ArrayList<>(); }

			sections.add(dwSection);
			dwSharedSectionGroups.put(dwSection.getSectionGroupSortingKey(), sections);
		}

		// Loop over shared section groups and look for activities that are 'shared'
		List<String> sharedActivityKeys = new ArrayList<>();
		List<String> createdSharedActivityKeys = new ArrayList<>();

		for (String dwSectionKey : dwSharedSectionGroups.keySet()) {
			Map<String, Long> activityKeyCounts = new HashMap<String, Long>();
			List<String> activityKeys = new ArrayList<>();

			for (DwSection dwSection : dwSharedSectionGroups.get(dwSectionKey)) {
				for (DwActivity dwActivity : dwSection.getActivities()) {
					String activityKey = dwActivity.getActivitySortingKey(dwSection.getSectionGroupSortingKey());

					if (activityKeys.indexOf(activityKey) == -1) {
						activityKeys.add(activityKey);
					}

					Long activityCount = activityKeyCounts.get(activityKey) != null ? activityKeyCounts.get(activityKey) : 0L;
					activityCount += 1;
					activityKeyCounts.put(activityKey, activityCount);
				}
			}

			for (String activityKey : activityKeys) {
				// If we found an activity in a shared section group's sections the same number of times
				// as there are sections in that shared section group, that activity is perfectly shared
				// amongst all of them, and therefore a shared activity.
				if (activityKeyCounts.get(activityKey) == dwSharedSectionGroups.get(dwSectionKey).size()) {
					sharedActivityKeys.add(activityKey);
				}
			}
		}

		// Begin the actual import now that shared section groups and shared activites have been identified
		for (SectionGroupImport sectionGroupImport : sectionGroupImportList) {
			for (DwSection dwSection : dwSections) {
				// Calculate sequencePattern from sequenceNumber
				Character c = dwSection.getSequenceNumber().charAt(0);
				String dwSequencePattern = Character.isLetter(c) ? String.valueOf(c) : dwSection.getSequenceNumber();

				// Compare termCode endings
				String sectionGroupImportShortTerm = sectionGroupImport.getTermCode().substring(sectionGroupImport.getTermCode().length() - 2);
				String dwSectionShortTerm = dwSection.getTermCode().substring(dwSection.getTermCode().length() - 2);

				if (((sectionGroupImport.getCourseNumber().equals( dwSection.getCourseNumber() )
				&& sectionGroupImport.getSubjectCode().equals( dwSection.getSubjectCode() )
				&& sectionGroupImport.getSequencePattern().equals( dwSequencePattern )
				&& sectionGroupImportShortTerm.equals(dwSectionShortTerm))) == false) {
					continue;
				}

				// Find or create a course
				String newTermCode;
				String shortTermCode = dwSection.getTermCode().substring(dwSection.getTermCode().length() - 2);

				// Identify which year in academic year range to use
				if (Long.valueOf(shortTermCode) < 4) {
					long nextYear = year + 1;
					newTermCode = nextYear + shortTermCode;
				} else {
					newTermCode = year + shortTermCode;
				}

				if (termHashMap.get(newTermCode) == null) {
					termHashMap.put(newTermCode, termService.getOneByTermCode(newTermCode));
				}

				Term term = termHashMap.get(newTermCode);

				Course courseDTO = new Course();
				courseDTO.setSubjectCode(sectionGroupImport.getSubjectCode());
				courseDTO.setCourseNumber(sectionGroupImport.getCourseNumber());
				courseDTO.setSequencePattern(sectionGroupImport.getSequencePattern());
				courseDTO.setTitle(sectionGroupImport.getTitle());
				courseDTO.setEffectiveTermCode(sectionGroupImport.getEffectiveTermCode());
				courseDTO.setSchedule(schedule);

				Float unitsHigh, unitsLow;

				if (sectionGroupImport.getUnitsHigh() != null) {
					unitsHigh = Float.valueOf(sectionGroupImport.getUnitsHigh());
					courseDTO.setUnitsHigh(unitsHigh);
				}

				if (sectionGroupImport.getUnitsLow() != null) {
					unitsLow = Float.valueOf(sectionGroupImport.getUnitsLow());
					courseDTO.setUnitsLow(unitsLow);
				}

				Course course = courseService.findOrCreateByCourse(courseDTO);

				// Find or create a sectionGroup
				SectionGroup sectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(course.getId(), newTermCode);
				sectionGroup.setPlannedSeats(sectionGroupImport.getPlannedSeats());
				sectionGroup = sectionGroupService.save(sectionGroup);

				// Find or create a section
				Section section = sectionService.findOrCreateBySectionGroupAndSequenceNumber(sectionGroup, dwSection.getSequenceNumber());

				section.setSeats(dwSection.getMaximumEnrollment());
				section.setCrn(dwSection.getCrn());
				section = sectionService.save(section);

				// Make activities
				for (DwActivity dwActivity : dwSection.getActivities()) {
					Activity activity = new Activity();

					activity.setActivityTypeCode(dwActivity.getSsrmeet_schd_code());

					if (importTimes) {
						activity.setStartTime(dwActivity.castBeginTime());
						activity.setEndTime(dwActivity.castEndTime());
						activity.setDayIndicator(dwActivity.getDay_indicator());
					}

					activity.setBeginDate(term.getStartDate());
					activity.setEndDate(term.getEndDate());
					activity.setActivityState(ActivityState.DRAFT);

					// Activities in numeric sectionGroups should always be 'shared' activities
					String activityKey = dwActivity.getActivitySortingKey(dwSection.getSectionGroupSortingKey());

					if (sharedActivityKeys.indexOf(activityKey) == -1) {
						activity.setSection(section);
						activityService.saveActivity(activity);
					} else {
						if (createdSharedActivityKeys.indexOf(activityKey) == -1) {
							activity.setSectionGroup(sectionGroup);
							createdSharedActivityKeys.add(activityKey);
							activityService.saveActivity(activity);
						}
					}
				}

				if (importAssignments) {
					for (DwInstructor dwInstructor : dwSection.getInstructors()) {
						if (dwInstructor.getEmployeeId().equals("989999999")) {
							sectionGroup.setShowTheStaff(true);
							continue;
						}

						DwPerson dwPerson = dwRepository.getPersonByLoginId(dwInstructor.getLoginId());

						if ((dwPerson == null) || (dwPerson.getUserId() == null && dwPerson.getoFullName() == null)) {
							log.warn("getPersonByLoginId Response from DW returned null, for criterion = " + dwInstructor.getLoginId());
							continue;
						}

						String instructorEmail = dwPerson.getEmail();

						// Find or create an instructor
						Instructor instructor = instructorService.findOrCreate(dwInstructor.getFirstName(), dwInstructor.getLastName(), instructorEmail, dwInstructor.getLoginId(), workgroupId, dwInstructor.getEmployeeId());

						// Find or create a teachingAssignment
						TeachingAssignment teachingAssignment = teachingAssignmentService.findOrCreateOneBySectionGroupAndInstructor(sectionGroup, instructor);
						teachingAssignment.setApproved(true);
						teachingAssignmentService.saveAndAddInstructorType(teachingAssignment);
					}
				}
			}
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
		authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		if (sectionGroupImportList.size() == 0) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		String termCode = sectionGroupImportList.get(0).getTermCode();
		Long importYear = termService.getAcademicYearFromTermCode(termCode);

		Schedule historicalSchedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, importYear);
		Schedule destinationSchedule = this.scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, destinationYear);

		for (SectionGroupImport sectionGroupImport : sectionGroupImportList) {
			// Identify historical course this sectionGroupImport is based off
			Course historicalCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
				sectionGroupImport.getSubjectCode(),
				sectionGroupImport.getCourseNumber(),
				sectionGroupImport.getSequencePattern(),
				historicalSchedule.getId());

			// Does the destination schedule already have this course? if so do nothing
			// When we identify a single sectionGroupImport that is based on the course, we then import all the sectionGroups associated to that course
			Course destinationCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
				sectionGroupImport.getSubjectCode(),
				sectionGroupImport.getCourseNumber(),
				sectionGroupImport.getSequencePattern(),
				destinationSchedule.getId());

			// If course already exists, do nothing
			if (destinationCourse != null) {
				continue;
			}

			// Make a newCourse in the current term based on the historical course
			destinationCourse = courseService.findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
					sectionGroupImport.getSubjectCode(),
					sectionGroupImport.getCourseNumber(),
					sectionGroupImport.getSequencePattern(),
					sectionGroupImport.getTitle(),
					sectionGroupImport.getEffectiveTermCode(),
					destinationSchedule,
					true);

			// Find its sectionGroups, and find/create new versions of them
			for (SectionGroup historicalSectionGroup : historicalCourse.getSectionGroups()) {

				String newTermCode = null;
				String shortTermCode = historicalSectionGroup.getTermCode().substring(4, 6);

				if (Long.valueOf(shortTermCode) < 4) {
					long nextYear = destinationYear + 1;
					newTermCode = nextYear + shortTermCode;
				} else {
					newTermCode = destinationYear + shortTermCode;
				}

				Term term = termService.getOneByTermCode(newTermCode);

				SectionGroup newSectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(destinationCourse.getId(), newTermCode);
				newSectionGroup.setPlannedSeats(historicalSectionGroup.getPlannedSeats());
				newSectionGroup = sectionGroupService.save(newSectionGroup);

				for (Section historicalSection : historicalSectionGroup.getSections()) {
					Section newSection = sectionService.findOrCreateBySectionGroupAndSequenceNumber(newSectionGroup, historicalSection.getSequenceNumber());
					newSection.setSeats(historicalSection.getSeats());
					newSection = sectionService.save(newSection);

					for (Activity historicalActivity : historicalSection.getActivities()) {
						Activity newActivity = new Activity();

						newActivity.setActivityTypeCode(historicalActivity.getActivityTypeCode());
						newActivity.setSection(newSection);

						if (importTimes) {
							newActivity.setDayIndicator(historicalActivity.getDayIndicator());
							newActivity.setStartTime(historicalActivity.getStartTime());
							newActivity.setEndTime(historicalActivity.getEndTime());
						}

						newActivity.setBeginDate(term.getStartDate());
						newActivity.setEndDate(term.getEndDate());
						newActivity.setActivityState(ActivityState.DRAFT);
						newActivity.setCategory(historicalActivity.getCategory());
						activityService.saveActivity(newActivity);
					}
				}

				if (importAssignments) {
					for (TeachingAssignment historicalTeachingAssignment : historicalSectionGroup.getTeachingAssignments()) {
						if (historicalTeachingAssignment.isApproved()) {
							TeachingAssignment newTeachingAssignment = new TeachingAssignment();

							newTeachingAssignment.setApproved(true);
							newTeachingAssignment.setFromInstructor(historicalTeachingAssignment.isFromInstructor());
							newTeachingAssignment.setInstructor(historicalTeachingAssignment.getInstructor());
							newTeachingAssignment.setInstructorType(historicalTeachingAssignment.getInstructorType());
							newTeachingAssignment.setSchedule(newSectionGroup.getCourse().getSchedule());
							newTeachingAssignment.setSectionGroup(newSectionGroup);
							newTeachingAssignment.setTermCode(newSectionGroup.getTermCode());

							teachingAssignmentService.saveAndAddInstructorType(newTeachingAssignment);
						}
					}
				}

				for (Activity historicalActivity : historicalSectionGroup.getActivities()) {
					Activity newActivity = new Activity();

					newActivity.setActivityTypeCode(historicalActivity.getActivityTypeCode());
					newActivity.setSectionGroup(newSectionGroup);

					if (importTimes) {
						newActivity.setDayIndicator(historicalActivity.getDayIndicator());
						newActivity.setStartTime(historicalActivity.getStartTime());
						newActivity.setEndTime(historicalActivity.getEndTime());
					}

					newActivity.setBeginDate(term.getStartDate());
					newActivity.setEndDate(term.getEndDate());
					newActivity.setActivityState(ActivityState.DRAFT);
					activityService.saveActivity(newActivity);
				}

			}
		}

		return annualViewFactory.createCourseView(workgroupId, destinationYear, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/queryCourses", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<JpaAnnualViewFactory.HistoricalCourse> queryCourses(
									@PathVariable long workgroupId,
									@PathVariable long year,
									@RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint) {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return annualViewFactory.createCourseQueryView(workgroupId, year, showDoNotPrint);
	}

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/generateExcel", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, String> generateExcel(@PathVariable long workgroupId, @PathVariable long year,
							 @RequestParam(value="showDoNotPrint", required=false) Boolean showDoNotPrint,
							 HttpServletRequest httpRequest) throws Exception {
		authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

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

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}/courses/{courseId}/sectionGroups/{sectionGroupId}/convert/{sequencePattern}", method = RequestMethod.POST, produces="application/json")
	@ResponseBody
	public List<Object> convertCourseOffering(@PathVariable Long workgroupId, @PathVariable Long year, @PathVariable Long courseId, @PathVariable Long sectionGroupId, @PathVariable String sequencePattern, HttpServletResponse httpResponse) {
		authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");
		Schedule schedule = this.scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

		Course existingCourse = courseService.getOneById(courseId);

		Course course = new Course();
		course.setSubjectCode(existingCourse.getSubjectCode());
		course.setCourseNumber(existingCourse.getCourseNumber());
		course.setSequencePattern(sequencePattern);
		course.setTitle(existingCourse.getTitle());
		course.setEffectiveTermCode(existingCourse.getEffectiveTermCode());
		course.setSchedule(schedule);
		course.setUnitsHigh(existingCourse.getUnitsHigh());
		course.setUnitsLow(existingCourse.getUnitsLow());

		Course newCourse = courseService.findOrCreateByCourse(course);

		SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

		// Make sure course doesn't already have an offering
		for (SectionGroup existingSectionGroup : newCourse.getSectionGroups()){
			if (existingSectionGroup.getTermCode().equals(sectionGroup.getTermCode())){
				httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
		}

		// Update Live Data
		List<SectionGroupCost> sectionGroupCosts = sectionGroupCostService.findBySectionGroupDetails(
				workgroupId,
				existingCourse.getYear(),
				existingCourse.getCourseNumber(),
				existingCourse.getSequencePattern(),
				existingCourse.getSubjectCode()
		);
		for(SectionGroupCost sectionGroupCost : sectionGroupCosts){
			if(sectionGroupCost.isLiveData()){
				sectionGroupCost.setSequencePattern(course.getSequencePattern());
				sectionGroupCostService.update(sectionGroupCost);
			}

		}
		Long seatCount = new Long(0);
		for(Section oldSection : sectionGroup.getSections()){
			seatCount += oldSection.getSeats();
			sectionService.deleteWithCascade(oldSection);
		}


		sectionGroup.setCourse(newCourse);
		sectionGroup.setPlannedSeats(seatCount.intValue());
		SectionGroup newSectionGroup = sectionGroupService.save(sectionGroup);

		Section section = new Section();
		if(sequencePattern.length() > 1){
			section.setSequenceNumber(sequencePattern);
		} else {
			section.setSequenceNumber(sequencePattern+"01");
		}
		section.setSeats(seatCount);
		section.setSectionGroup(sectionGroup);
		sectionService.save(section);

		if (newCourse != null) {
			return Arrays.asList(newCourse, newSectionGroup);
		} else {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}
	}
}
