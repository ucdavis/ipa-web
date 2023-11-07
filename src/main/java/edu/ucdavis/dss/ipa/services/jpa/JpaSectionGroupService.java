package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.services.*;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JpaSectionGroupService implements SectionGroupService {
	@Inject SectionGroupRepository sectionGroupRepository;

	@Inject ScheduleTermStateService scheduleTermStateService;
	@Inject ScheduleService scheduleService;
	@Inject SectionService sectionService;
	@Inject CourseService courseService;
	@Inject InstructorService instructorService;
	@Inject TermService termService;
	@Inject SupportAssignmentService supportAssignmentService;
	@Inject StudentSupportCallResponseService studentSupportCallResponseService;
	@Inject ActivityService activityService;
	@Inject EmailService emailService;

	@Override
	@Transactional
	//@PreAuthorize("hasAnyRole('admin','academicCoordinator')")
	public SectionGroup getOneById(Long id) {
		return sectionGroupRepository.findById(id).orElse(null);
	}

	@Override
	public SectionGroup save(SectionGroup sectionGroup) {
		return this.sectionGroupRepository.save(sectionGroup);
	}

	@Override
	public void delete(Long id) {
		this.sectionGroupRepository.delete(id);
	}

	@Override
	public List<SectionGroup> findEmpty() {
		return this.sectionGroupRepository.findEmpty();
	}

	@Override
	public List<SectionGroup> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
		Schedule schedule = this.scheduleService.findById(scheduleId);
		List<SectionGroup> sectionGroups = schedule.getCourses()
				.stream()
				.map(course -> course.getSectionGroups().stream()
						.filter(sectionGroup -> termCode == null || termCode.trim().isEmpty() || sectionGroup.getTermCode().equals(termCode.trim()))
						.collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return sectionGroups;
	}

	@Override
	public List<SectionGroup> findByScheduleId(long scheduleId) {
		Schedule schedule = this.scheduleService.findById(scheduleId);
		List<SectionGroup> sectionGroups = schedule.getCourses()
				.stream()
				.map(course -> course.getSectionGroups().stream()
						.collect(Collectors.toList()))
				.flatMap(Collection::stream)
				.collect(Collectors.toList());
		return sectionGroups;
	}

	@Override
	public List<SectionGroup> findByWorkgroupIdAndYear(long workgroupId, long year) {
		return sectionGroupRepository.findByCourseScheduleWorkgroupIdAndCourseScheduleYear(workgroupId, year);
	}

	@Override
	public List<SectionGroup> findByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		return sectionGroupRepository.findByCourseScheduleWorkgroupIdAndCourseScheduleYearAndTermCode(workgroupId, year, termCode);
	}

	@Override
	public List<SectionGroup> findVisibleByWorkgroupIdAndYearAndTermCode(long workgroupId, long year, String termCode) {
		List<SectionGroup> occupiedVisibleSectionGroups = sectionGroupRepository.findOccupiedVisibleByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		List<SectionGroup> emptySectionGroups = sectionGroupRepository.findEmptyByWorkgroupIdAndYearAndTermCode(workgroupId, year, termCode);
		occupiedVisibleSectionGroups.addAll(emptySectionGroups);
		Collections.sort(occupiedVisibleSectionGroups, (o1, o2) -> o1.getCourse().getShortDescription().compareTo(o2.getCourse().getShortDescription()));
		return occupiedVisibleSectionGroups;
	}

	@Override
	public List<SectionGroup> findByCourse(Course course) {
		return this.sectionGroupRepository.findByCourseId(course.getId());
	}

	@Override
	public List<SectionGroup> findByCourses(List<Course> courses) {
		List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();

		for (Course course : courses) {
			sectionGroups.addAll(course.getSectionGroups());
		}

		return sectionGroups;
	}

	@Override
	public SectionGroup findBySectionGroupCostDetails(long workgroupId, String courseNumber, String sequencePattern, String termCode, String subjectCode){
		return sectionGroupRepository.findBySectionGroupCostDetails(workgroupId, courseNumber, sequencePattern, termCode, subjectCode);
	}

	/**
	 * Returns sectionGroups that have an support assignment for a type that is relevant to the specified student support call.
	 * @param scheduleId
	 * @param termCode
	 * @param studentSupportCallResponseId
     * @return
     */
	@Override
	public List<SectionGroup> findByScheduleIdAndTermCodeAndStudentSupportCallResponseId(long scheduleId, String termCode, long studentSupportCallResponseId) {
		List<SectionGroup> allSectionGroups = this.findByScheduleIdAndTermCode(scheduleId, termCode);
		List<SupportAssignment> supportAssignments = supportAssignmentService.findByScheduleIdAndTermCode(scheduleId, termCode);
		StudentSupportCallResponse studentSupportCallResponse = studentSupportCallResponseService.findOneById(studentSupportCallResponseId);

		// List of sectionGroups that are valid options to be preferences
		List<SectionGroup> filteredSectionGroups = new ArrayList<>();

		// Loop over sectionGroups in schedule/term
		for ( SectionGroup slotSectionGroup : allSectionGroups) {

			for (SupportAssignment slotSupportAssignment : supportAssignments) {

				// Assignment is for this sectionGroupId and matches one of the interested types from the support call?
				if (slotSupportAssignment.getSectionGroup().getId() == slotSectionGroup.getId()
				&& isSupportCallAssignmentMatch(studentSupportCallResponse, slotSupportAssignment)) {
					filteredSectionGroups.add(slotSectionGroup);
					break;
				}
			}
		}

		return filteredSectionGroups;
	}

	@Override
	public SectionGroup findOrCreateByCourseIdAndTermCode(Long courseId, String termCode) {
		SectionGroup sectionGroup = sectionGroupRepository.findByCourseIdAndTermCode(courseId, termCode);
		Course course = this.courseService.getOneById(courseId);

		if (course == null) {
			return null;
		}

		if (sectionGroup == null) {
			sectionGroup = new SectionGroup();
			sectionGroup.setTermCode(termCode);
			sectionGroup.setCourse(course);
			sectionGroup = sectionGroupRepository.save(sectionGroup);
		}

		return sectionGroup;
	}

	/**
	 * Look through the sectionGroup for activities that exist on every section, remove them, and put them on the sharedActivities list
	 * @param sectionGroup
     */
	@Override
	public SectionGroup identifyAndCondenseSharedActivities(SectionGroup sectionGroup) {

		List<String> potentialSharedActivityKeys = new ArrayList<>();
		Boolean isFirstSection = true;

		// Identify activities that are shared among all sections
		for (Section section : sectionGroup.getSections()) {
			List<String> sectionKeys = new ArrayList<>();

			for (Activity activity : section.getActivities()) {
				// Example 'D-0101100-13:10:00-14:00:00'
				String sectionKey = activity.getActivityTypeCode().getActivityTypeCode() + "-" +
						activity.getDayIndicator() + "-" +
						activity.getStartTime() + "-" +
						activity.getEndTime();
				sectionKeys.add(sectionKey);
			}

			if (isFirstSection) {
				// On first section all sectionKeys are potential
				potentialSharedActivityKeys.addAll(sectionKeys);
				isFirstSection = false;
			} else {
				// Loop over potentialSharedActivityKeys and ensure each exists in sectionKeys
				List<String> verifiedKeys = new ArrayList<>();

				for (String potentialKey : potentialSharedActivityKeys) {
					for (String sectionKey : sectionKeys) {
						if (sectionKey.equals(potentialKey)) {
							verifiedKeys.add(potentialKey);
						}
					}
				}

				potentialSharedActivityKeys = verifiedKeys;
			}
		}

		// Remove sharedActivities from each section, and add it to the sectionGroup
		for (String key : potentialSharedActivityKeys) {
			Activity sharedActivity = null;

			for (Section section : sectionGroup.getSections()) {
				for (Activity activity : section.getActivities()) {
					// Example 'D-0101100-13:10:00-14:00:00'
					String activityKey = activity.getActivityTypeCode().getActivityTypeCode() + "-" +
							activity.getDayIndicator() + "-" +
							activity.getStartTime() + "-" +
							activity.getEndTime();

					if (activityKey.equals(key)) {
						if (sharedActivity == null) {
							// Create shared activity on the sectionGroup
							sharedActivity = new Activity();
							sharedActivity.setSectionGroup(sectionGroup);
							sharedActivity.setDayIndicator(activity.getDayIndicator());
							sharedActivity.setStartTime(activity.getStartTime());
							sharedActivity.setEndTime(activity.getEndTime());
							sharedActivity.setBeginDate(activity.getBeginDate());
							sharedActivity.setEndDate(activity.getEndDate());
							sharedActivity.setBannerLocation(activity.getBannerLocation());
							sharedActivity.setLocation(activity.getLocation());
							sharedActivity.setActivityTypeCode(activity.getActivityTypeCode());
							sharedActivity = activityService.saveActivity(sharedActivity);
						}

						// Remove activity from section
						List<Activity> activities = section.getActivities();
						int index = activities.indexOf(activity);
						activities.remove(index);
						section.setActivities(activities);
						sectionService.save(section);

						// Delete activity
						activityService.deleteActivityById(activity.getId());
					}
				}
			}
		}

		return sectionGroup;
	}

	/**
	 * Returns true if the assignment is a type of interest to the support call
	 * @param studentSupportCallResponse
	 * @param supportAssignment
     * @return
     */
	private boolean isSupportCallAssignmentMatch (StudentSupportCallResponse studentSupportCallResponse, SupportAssignment supportAssignment) {
		if (studentSupportCallResponse.isCollectAssociateInstructorPreferences()) {
			if (supportAssignment.getAppointmentType().equals("associateInstructor")) {
				return true;
			}
		}

		if (studentSupportCallResponse.isCollectReaderPreferences()) {
			if (supportAssignment.getAppointmentType().equals("reader")) {
				return true;
			}
		}

		if (studentSupportCallResponse.isCollectTeachingAssistantPreferences()) {
			if (supportAssignment.getAppointmentType().equals("teachingAssistant")) {
				return true;
			}
		}

		return false;
	}

	private boolean isLocked(long sectionGroupId) {
		SectionGroup sectionGroup = this.getOneById(sectionGroupId);
		if (sectionGroup == null) { return false; }

		Term term = termService.getOneByTermCode(sectionGroup.getTermCode());
		ScheduleTermState termState = this.scheduleTermStateService.createScheduleTermState(term);

		if (termState != null && termState.scheduleTermLocked()) {
			emailService.reportException(new UnsupportedOperationException("Term " + sectionGroup.getTermCode() + " is locked"), this.getClass().getName());
			return true;
		}

		return false;
	}

}
