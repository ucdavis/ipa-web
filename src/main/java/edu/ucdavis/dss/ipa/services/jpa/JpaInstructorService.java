package edu.ucdavis.dss.ipa.services.jpa;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.InstructorRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructorService implements InstructorService {
	@Inject InstructorRepository instructorRepository;
	@Inject WorkgroupService workgroupService;
	@Inject ScheduleService scheduleService;
	@Inject UserRoleService userRoleService;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Instructor save(Instructor instructor) {
		return this.instructorRepository.save(instructor);
	}

	@Override
	public Instructor getOneById(Long id) {
		return this.instructorRepository.findById(id);
	}

	@Override
	public Instructor getOneByUcdStudentSID(String ucdStudentSID) {
		return this.instructorRepository.findByUcdStudentSID(ucdStudentSID);
	}

	@Override
	public Instructor getOneByLoginId(String loginId) {
		return this.instructorRepository.findByLoginIdIgnoreCase(loginId);
	}

	/**
	 * Overrides findOrCreate without employeeId.
	 * Currently, employeeId is desired for instructor creation but not reliably available from DW.
	 */
	@Override
	public Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId) {
		return this.findOrCreate(firstName, lastName, email, loginId, workgroupId, "");
	}

	@Override
	public Instructor findOrCreate(String firstName, String lastName, String email, String loginId, Long workgroupId, String employeeId) {

		// 1) Attempt to find by loginId
		Instructor instructor = instructorRepository.findByLoginIdIgnoreCase(loginId);

		// 2) Attempt to find by employeeId
		if (instructor == null && employeeId.length() > 0) {
			instructor = instructorRepository.findByUcdStudentSID(employeeId);
		}

		// 3) Create new instructor
		if (instructor == null) {
			instructor = new Instructor();
			
			instructor.setFirstName(firstName);
			instructor.setLastName(lastName);
			instructor.setEmail(email);
			instructor.setLoginId(loginId);
		}
		
		instructorRepository.save(instructor);

		return instructor;
	}

	@Override
	public void removeOrphanedByLoginId(String loginId) {
		Instructor instructor = this.getOneByLoginId(loginId);

		if (instructor == null) {
			log.warn("Attempting to find instructor by loginId: " +loginId + ", was not found.");
			return;
		}

		if (instructor.getTeachingAssignments().size() == 0 &&
			instructor.getTeachingCallResponses().size() == 0 &&
			instructor.getTeachingCallReceipts().size() == 0) {
				instructorRepository.delete(instructor.getId());
		}
	}

	@Override
	public List<Instructor> findByScheduleId(long scheduleId) {
		Schedule schedule = scheduleService.findById(scheduleId);

		List<SectionGroup> sectionGroups = new ArrayList<>();

		for (Course course : schedule.getCourses()) {
			sectionGroups.addAll(course.getSectionGroups());
		}

		List<Instructor> activeInstructors = userRoleService.getInstructorsByWorkgroupId(schedule.getWorkgroup().getId());
		List<Instructor> instructorsFromAssignments = this.findBySectionGroups(sectionGroups);

		List<Long> uniqueInstructorIds = new ArrayList<>();
		List<Instructor> uniqueInstructors = new ArrayList<>();

		for (Instructor slotInstructor : instructorsFromAssignments) {
			if (uniqueInstructorIds.indexOf(slotInstructor.getId()) == -1) {
				uniqueInstructorIds.add(slotInstructor.getId());
				uniqueInstructors.add(slotInstructor);
			}
		}

		for (Instructor slotInstructor : activeInstructors) {
			if (uniqueInstructorIds.indexOf(slotInstructor.getId()) == -1) {
				uniqueInstructorIds.add(slotInstructor.getId());
				uniqueInstructors.add(slotInstructor);
			}
		}

		return uniqueInstructors;
	}

	private List<Instructor> findBySectionGroups(List<SectionGroup> sectionGroups) {
		List<Long> uniqueInstructorIds = new ArrayList<>();
		List<Instructor> uniqueInstructors = new ArrayList<>();

		for(SectionGroup sectionGroup : sectionGroups) {
			for (TeachingAssignment teachingAssignment : sectionGroup.getTeachingAssignments()) {
				Instructor slotInstructor = teachingAssignment.getInstructor();
				if (uniqueInstructorIds.indexOf(slotInstructor.getId()) == -1) {
					uniqueInstructors.add(slotInstructor);
					uniqueInstructorIds.add(slotInstructor.getId());
				}
			}
		}

		return uniqueInstructors;
	}

	/**
	 * Find all instructors associated to active 'instructor' type users in the workgroup.
	 * @param workgroupId
	 * @return
     */
	public List<Instructor> findActiveByWorkgroupId(long workgroupId) {
		List<Instructor> activeInstructors = new ArrayList<Instructor>();

		Workgroup workgroup = workgroupService.findOneById(workgroupId);

		List<UserRole> userRoles = workgroup.getUserRoles();

		for (UserRole userRole : userRoles) {
			if (userRole.getRoleToken().equals("senateInstructor") || userRole.getRoleToken().equals("federationInstructor") || userRole.getRoleToken().equals("lecturer")) {
				String loginId = userRole.getUser().getLoginId();

				Instructor slotInstructor = this.getOneByLoginId(loginId);
				activeInstructors.add(slotInstructor);
			}
		}

		return activeInstructors;
	}

	@Override
	public Instructor findOrAddActiveInstructor(Workgroup workgroup, User user) {
		// Create Instructor
		Instructor instructor = this.findOrCreate(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLoginId(), workgroup.getId());

		// Create userRole, if necessary
		userRoleService.findOrAddInstructorRoleToWorkgroup(workgroup, user);

		return instructor;
	}

	@Override
	public List<Instructor> findAssignedByScheduleId(long scheduleId) {
		Schedule schedule = scheduleService.findById(scheduleId);

		if (schedule == null) {
			return null;
		}

		List<Instructor> instructors = new ArrayList<>();

		for (TeachingAssignment teachingAssignment : schedule.getTeachingAssignments()) {
			if (teachingAssignment.isApproved()) {
				instructors.add(teachingAssignment.getInstructor());
			}
		}

		return instructors;
	}

	@Override
	public List<Instructor> findActiveByWorkgroupIdAndLecturer(long workgroupId, boolean isLecturer) {
		List<Instructor> activeInstructors = new ArrayList<Instructor>();
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		List<UserRole> userRoles = workgroup.getUserRoles();

		for (UserRole userRole : userRoles) {
			if (isLecturer) {
				if (userRole.getRoleToken().equals("lecturer")) {
					String loginId = userRole.getUser().getLoginId();

					Instructor slotInstructor = this.getOneByLoginId(loginId);
					activeInstructors.add(slotInstructor);
				}
			} else {
				if (userRole.getRoleToken().equals("senateInstructor") || userRole.getRoleToken().equals("federationInstructor") || userRole.getRoleToken().equals("lecturer")) {
					String loginId = userRole.getUser().getLoginId();

					Instructor slotInstructor = this.getOneByLoginId(loginId);
					activeInstructors.add(slotInstructor);
				}
			}
		}

		return activeInstructors;
	}

	@Override
	public List<Instructor> findByInstructorCosts(List<InstructorCost> instructorCosts) {
		List<Instructor> instructors = new ArrayList<>();

		for (InstructorCost instructorCost : instructorCosts) {
			instructors.add(instructorCost.getInstructor());
		}

		return instructors;
	}
}