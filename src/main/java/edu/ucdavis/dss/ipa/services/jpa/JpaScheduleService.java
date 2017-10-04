package edu.ucdavis.dss.ipa.services.jpa;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import edu.ucdavis.dss.dw.dto.DwActivity;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.ipa.api.components.course.views.SectionGroupImport;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.entities.enums.ActivityState;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.ScheduleRepository;

@Service
public class JpaScheduleService implements ScheduleService {
	@Inject ScheduleRepository scheduleRepository;
	@Inject WorkgroupService workgroupService;
	@Inject UserService userService;
	@Inject TermService termService;
	@Inject DataWarehouseRepository dwRepository;
	@Inject CourseService courseService;
	@Inject ActivityService activityService;
	@Inject SectionGroupService sectionGroupService;
	@Inject SectionService sectionService;
	@Inject TeachingAssignmentService teachingAssignmentService;

	@Override
	public Schedule saveSchedule(Schedule schedule) {
		return this.scheduleRepository.save(schedule);
	}

	@Override
	public Schedule findById(long id) {
		return this.scheduleRepository.findOne(id);
	}

	@Override
	@Transactional
	public Schedule createSchedule(Long workgroupId, long year) {
		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);
		if(workgroup == null) return null;

		Schedule schedule = new Schedule();
		schedule.setWorkgroup(workgroup);
		schedule.setImporting(false);

		if (year != 0L) {
			schedule.setYear(year);
		} else {
			schedule.setYear(Calendar.getInstance().get(Calendar.YEAR));
		}

		return this.saveSchedule(schedule);
	}

	@Override
	@Transactional
	public Workgroup getWorkgroupByScheduleId(Long scheduleId) {
		Schedule schedule = this.scheduleRepository.findOne(scheduleId);
		if (schedule != null) {
			Workgroup d = schedule.getWorkgroup();
			Hibernate.initialize(d);
			return d;
		}
		return null;
	}

	@Override
	public List<User> getUserInstructorsByScheduleIdAndTermCode(Long scheduleId, String termCode) {
		List<User> users = new ArrayList<User>();
		Schedule schedule = this.findById(scheduleId);

		for(TeachingAssignment teachingAssignment : schedule.getTeachingAssignments() ) {

			if( teachingAssignment.isApproved() && teachingAssignment.getTermCode().equals(termCode)) {

				String loginId = teachingAssignment.getInstructor().getLoginId();
				User user = userService.getOneByLoginId(loginId);

				if (user != null) {
					users.add(user);
				}

			}
		}
		return users;
	}

	@Override
	public Schedule findByWorkgroupIdAndYear(long workgroupId, long year) {
		return scheduleRepository.findOneByYearAndWorkgroupWorkgroupId(workgroupId, year);
	}

	@Override
	public Schedule findOrCreateByWorkgroupIdAndYear(long workgroupId, long year) {
		Schedule schedule = this.findByWorkgroupIdAndYear(workgroupId, year);
		if (schedule == null) {
			schedule = this.createSchedule(workgroupId, year);
		}
		return schedule;
	}

	@Override
	public boolean deleteByScheduleId(long scheduleId) {
		scheduleRepository.delete(scheduleId);
		return true;
	}

	@Override
	public boolean isScheduleClosed(long scheduleId) {
		Schedule schedule = this.findById(scheduleId);
		Date now = new Date();
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(now);

		// If the schedule year is after the current year, it is definitely not closed.
		if(schedule.getYear() > calendar.get(Calendar.YEAR)) return false;
		
		// Else, if any term code in this schedule ends after today, the schedule is not closed.
		Set<String> termCodes = Term.getTermCodesByYear(schedule.getYear());
		List<Term> terms = this.termService.findByTermCodeInAndExistingEndDateAfterNow(termCodes);
		
		return terms.size() == 0;
	}

	@Override
	public List<Term> getActiveTermCodesForSchedule(Schedule schedule) {
		return this.scheduleRepository.getActiveTermsForScheduleId(schedule.getId());
	}

	@Override
	public boolean createMultipleCoursesFromDw(Schedule schedule, List<SectionGroupImport> sectionGroupImportList, Boolean importTimes, Boolean importAssignments) {
		// Method currently implies all requested sectionGroups have the same subjectCode
		String subjectCode = sectionGroupImportList.get(0).getSubjectCode();

		// Calculate academicYear from the termCode of the first sectionGroupImport
		String termCode = sectionGroupImportList.get(0).getTermCode();
		Long yearToImportFrom = termService.getAcademicYearFromTermCode(termCode);

		List<DwSection> dwSections = dwRepository.getSectionsBySubjectCodeAndYear(subjectCode, yearToImportFrom);

		for (SectionGroupImport sectionGroupImport : sectionGroupImportList) {

			for (DwSection dwSection : dwSections) {
				String newTermCode = null;
				String shortTermCode = dwSection.getTermCode().substring(4, 6);

				if (Long.valueOf(shortTermCode) < 4) {
					long nextYear = schedule.getYear() + 1;
					newTermCode = nextYear + shortTermCode;
				} else {
					newTermCode = schedule.getYear() + shortTermCode;
				}

				Term term = termService.getOneByTermCode(newTermCode);

				// Calculate sequencePattern from sequenceNumber
				String dwSequencePattern = null;

				Character c = dwSection.getSequenceNumber().charAt(0);
				Boolean isLetter = Character.isLetter(c);
				if (isLetter) {
					dwSequencePattern = String.valueOf(c);
				} else {
					dwSequencePattern = dwSection.getSequenceNumber();
				}

				// Compare termCode endings
				String sectionGroupImportShortTerm = sectionGroupImport.getTermCode().substring(sectionGroupImport.getTermCode().length() - 2);
				String dwSectionShortTerm = dwSection.getTermCode().substring(dwSection.getTermCode().length() - 2);

				// Ensure this dwSection matches the sectionGroupImport (course) of interest
				if (sectionGroupImport.getCourseNumber().equals( dwSection.getCourseNumber() )
						&& sectionGroupImport.getSubjectCode().equals( dwSection.getSubjectCode() )
						&& sectionGroupImport.getSequencePattern().equals( dwSequencePattern )
						&& sectionGroupImportShortTerm.equals(dwSectionShortTerm)) {

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

					if (sectionGroupImport.getUnitsHigh() != null) {
						course.setUnitsHigh(Long.valueOf(sectionGroupImport.getUnitsHigh()));
					}

					if (sectionGroupImport.getUnitsLow() != null) {
						course.setUnitsLow(Long.valueOf(sectionGroupImport.getUnitsLow()));
					}

					course = courseService.update(course);

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

						if (importTimes) {
							String rawStartTime = dwActivity.getSsrmeet_begin_time();

							if (rawStartTime != null) {
								String hours = rawStartTime.substring(0, 2);
								String minutes = rawStartTime.substring(2, 4);
								String formattedStartTime = hours + ":" + minutes + ":00";
								Time startTime = java.sql.Time.valueOf(formattedStartTime);

								activity.setStartTime(startTime);
							}

							String rawEndTime = dwActivity.getSsrmeet_end_time();

							if (rawEndTime != null) {
								String hours = rawStartTime.substring(0, 2);
								String minutes = rawStartTime.substring(2, 4);
								String formattedEndTime = hours + ":" + minutes + ":00";
								Time endTime = java.sql.Time.valueOf(formattedEndTime);

								activity.setEndTime(endTime);
							}

							String dayIndicator = dwActivity.getDay_indicator();
							activity.setDayIndicator(dayIndicator);
						}

						activity.setBeginDate(term.getStartDate());
						activity.setEndDate(term.getEndDate());
						activity.setActivityState(ActivityState.DRAFT);

						// Activities in numeric sectionGroups should always be 'shared' activities
						if (Utilities.isNumeric(dwSequencePattern)) {
							activity.setSectionGroup(sectionGroup);
						} else {
							activity.setSection(section);
						}

						activityService.saveActivity(activity);
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean createMultipleCoursesFromIPA(Schedule schedule, List<SectionGroupImport> sectionGroupImportList, Boolean importTimes, Boolean importAssignments) {
		String termCode = sectionGroupImportList.get(0).getTermCode();
		Long importYear = termService.getAcademicYearFromTermCode(termCode);

		Schedule importSchedule = this.findOrCreateByWorkgroupIdAndYear(schedule.getWorkgroup().getId(), importYear);

		for (SectionGroupImport sectionGroupImport : sectionGroupImportList) {

			// Find course referenced by this sectionGroup
			Course historicalCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
					sectionGroupImport.getSubjectCode(),
					sectionGroupImport.getCourseNumber(),
					sectionGroupImport.getSequencePattern(),
					importSchedule.getId());

			if (historicalCourse == null) {
				continue;
			}

			// If course already exists, do nothing
			Course newCourse = courseService.findBySubjectCodeAndCourseNumberAndSequencePatternAndScheduleId(
					sectionGroupImport.getSubjectCode(),
					sectionGroupImport.getCourseNumber(),
					sectionGroupImport.getSequencePattern(),
					schedule.getId());

			if (newCourse != null) {
				continue;
			}

			// Make a newCourse in the current term based on the historical course
			newCourse = courseService.findOrCreateBySubjectCodeAndCourseNumberAndSequencePatternAndTitleAndEffectiveTermCodeAndScheduleId(
					sectionGroupImport.getSubjectCode(),
					sectionGroupImport.getCourseNumber(),
					sectionGroupImport.getSequencePattern(),
					sectionGroupImport.getTitle(),
					sectionGroupImport.getEffectiveTermCode(),
					schedule,
					true);

			// Find its sectionGroups, and find/create new versions of them
			for (SectionGroup historicalSectionGroup : historicalCourse.getSectionGroups()) {
				String newTermCode = null;
				String shortTermCode = historicalSectionGroup.getTermCode().substring(4, 6);

				if (Long.valueOf(shortTermCode) < 4) {
					long nextYear = schedule.getYear() + 1;
					newTermCode = nextYear + shortTermCode;
				} else {
					newTermCode = schedule.getYear() + shortTermCode;
				}

				Term term = termService.getOneByTermCode(newTermCode);

				SectionGroup newSectionGroup = sectionGroupService.findOrCreateByCourseIdAndTermCode(newCourse.getId(), newTermCode);
				newSectionGroup.setPlannedSeats(historicalSectionGroup.getPlannedSeats());
				newSectionGroup = sectionGroupService.save(newSectionGroup);

				for (Section historicalSection : historicalSectionGroup.getSections()) {
					Section newSection = sectionService.findOrCreateBySectionGroupIdAndSequenceNumber(newSectionGroup.getId(), historicalSection.getSequenceNumber());
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
							newTeachingAssignment.setSchedule(newSectionGroup.getCourse().getSchedule());
							newTeachingAssignment.setSectionGroup(newSectionGroup);
							newTeachingAssignment.setTermCode(newSectionGroup.getTermCode());

							teachingAssignmentService.save(newTeachingAssignment);
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

		return true;
	}

	@Override
	public List<Schedule> findAll() {
		return (List<Schedule>) this.scheduleRepository.findAll();
	}
}