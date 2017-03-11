package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.SectionGroupRepository;
import edu.ucdavis.dss.ipa.services.*;
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
	@Inject WorkgroupService workgroupService;
	@Inject TermService termService;
	@Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;
	@Inject InstructionalSupportAssignmentService instructionalSupportAssignmentService;

	@Override
	@Transactional
	//@PreAuthorize("hasAnyRole('admin','academicCoordinator')")
	public SectionGroup getOneById(Long id) {
		return sectionGroupRepository.findOne(id);
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
	public List<SectionGroup> findVisibleByWorkgroupIdAndYear(long workgroupId, long year) {
		List<Course> courses = courseService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
		List<SectionGroup> sectionGroups = new ArrayList<SectionGroup>();

		for (Course course : courses) {
			sectionGroups.addAll(course.getSectionGroups());
		}

		return sectionGroups;
	}

	/**
	 * Returns sectionGroups that have an support assignment for a type that is relevant to the specified student support call.
	 * @param scheduleId
	 * @param termCode
	 * @param studentSupportCallId
     * @return
     */
	@Override
	public List<SectionGroup> findByScheduleIdAndTermCodeAndStudentSupportCallId(long scheduleId, String termCode, long studentSupportCallId) {
		List<SectionGroup> allSectionGroups = this.findByScheduleIdAndTermCode(scheduleId, termCode);
		StudentSupportCall studentSupportCall = studentInstructionalSupportCallService.findOneById(studentSupportCallId);
		List<SupportAssignment> supportAssignments = instructionalSupportAssignmentService.findByScheduleIdAndTermCode(scheduleId, termCode);

		// List of sectionGroups that are valid options to be preferences
		List<SectionGroup> filteredSectionGroups = new ArrayList<>();

		// Loop over sectionGroups in schedule/term
		for ( SectionGroup slotSectionGroup : allSectionGroups) {

			for (SupportAssignment slotSupportAssignment : supportAssignments) {

				// Assignment is for this sectionGroupId and matches one of the interested types from the support call?
				if (slotSupportAssignment.getSectionGroup().getId() == slotSectionGroup.getId()
				&& isSupportCallAssignmentMatch(studentSupportCall, slotSupportAssignment)) {
					filteredSectionGroups.add(slotSectionGroup);
					break;
				}
			}
		}

		return filteredSectionGroups;
	}

	/**
	 * Returns true if the assignment is a type of interest to the support call
	 * @param studentSupportCall
	 * @param supportAssignment
     * @return
     */
	private boolean isSupportCallAssignmentMatch (StudentSupportCall studentSupportCall, SupportAssignment supportAssignment) {
		if (studentSupportCall.isCollectAssociateInstructorPreferences()) {
			if (supportAssignment.getAppointmentType().equals("associateInstructor")) {
				return true;
			}
		}

		if (studentSupportCall.isCollectReaderPreferences()) {
			if (supportAssignment.getAppointmentType().equals("reader")) {
				return true;
			}
		}

		if (studentSupportCall.isCollectTeachingAssistantPreferences()) {
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
			ExceptionLogger.logAndMailException(
					this.getClass().getName(),
					new UnsupportedOperationException("Term " + sectionGroup.getTermCode() + " is locked")
			);
			return true;
		}

		return false;
	}

}
