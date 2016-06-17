package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.repositories.ScheduleRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaScheduleService implements ScheduleService {
	@Inject ScheduleRepository scheduleRepository;
	@Inject WorkgroupService workgroupService;
	@Inject UserService userService;
	@Inject TermService termService;

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
	public List<SectionGroup> getSectionGroupsByScheduleIdAndSectionGroup(
			long id, SectionGroup sectionGroup) {
		Schedule schedule = this.findById(id);
		List<SectionGroup> SGs = new ArrayList<SectionGroup>();
		for (Course cog : schedule.getCourses()) {
			for (CourseOffering courseOffering : cog.getSectionGroups()) {
				for (SectionGroup sg : courseOffering.getSectionGroups()) {
					if (sg.getSubjectCode().equals(sectionGroup.getSubjectCode())
							&&	sg.getCourseNumber().equals(sectionGroup.getCourseNumber())
							&&	sg.getEffectiveTermCode().equals(sectionGroup.getEffectiveTermCode())) {
						SGs.add(sg);
					}
				}
			}
		}
		return SGs;
	}

	@Override
	public List<User> getUserInstructorsByScheduleIdAndTermCode(Long scheduleId, String termCode) {
		List<User> users = new ArrayList<User>();
		Schedule schedule = this.findById(scheduleId);

		for(TeachingPreference teachingPreference : schedule.getTeachingAssignments() ) {

			if( (teachingPreference.isApproved() == true) && teachingPreference.getTermCode().equals(termCode)) {

				String loginId = teachingPreference.getInstructor().getLoginId();
				User user = userService.getUserByLoginId(loginId);

				if (user != null) {
					users.add(user);
				}

			}
		}
		return users;
	}

	@Override
	public Schedule findByWorkgroupAndYear(Workgroup workgroup, long year) {
		if (workgroup != null) {
			return scheduleRepository.findOneByYearAndWorkgroupWorkgroupId(workgroup.getId(), year);
		} else {
			return null;
		}
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
	public List<String> getActiveTermCodesForSchedule(Schedule schedule) {
		return this.scheduleRepository.getActiveTermCodesForScheduleId(schedule.getId());
	}

	@Override
	public List<Schedule> findAll() {
		return (List<Schedule>) this.scheduleRepository.findAll();
	}
}