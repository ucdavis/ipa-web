package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.InstructorTypeRepository;
import edu.ucdavis.dss.ipa.repositories.RoleRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SupportStaffService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.UserRoleRepository;

@Service
public class JpaUserRoleService implements UserRoleService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject UserRoleRepository userRoleRepository;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject RoleService roleService;
	@Inject InstructorService instructorService;
	@Inject SupportStaffService supportStaffService;
	@Inject EmailService emailService;
	@Inject ScheduleService scheduleService;
	@Inject InstructorTypeRepository instructorTypeRepository;

	@Override
	@Transactional
	public UserRole save(UserRole userRole) {
		return this.userRoleRepository.save(userRole);
	}

	@Override
	public UserRole getOneById(Long id) {
		return this.userRoleRepository.findById(id).orElse(null);
	}

	@Override
	public List<UserRole> findByLoginIdAndWorkgroup(String loginId, Workgroup workgroup) {
		User user = userService.getOneByLoginId(loginId);

		if (user == null) { return null; }

		List<UserRole> workgroupUserRoles = new ArrayList<UserRole>();
		for( UserRole userRole : user.getUserRoles() ) {
			if ( userRole.getWorkgroup() != null && userRole.getWorkgroup().equals(workgroup) ) {
				workgroupUserRoles.add(userRole);
			}
		}
		return workgroupUserRoles;
	}

	@Override
	public UserRole findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String roleName) {
		User user = this.userService.findOrCreateByLoginId(loginId);
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Role role = roleService.findOneByName(roleName);

		if( user != null && workgroup != null && role != null) {
			// Find userRole if it exists
			for (UserRole userRole : this.findByLoginIdAndWorkgroup(loginId, workgroup)) {
				if ( userRole.getRole().equals(role) ) {
					return userRole;
				}
			}

			UserRole userRole = new UserRole();
			userRole.setWorkgroup(workgroup);
			userRole.setUser(user);
			userRole.setRole(role);

			if (roleName.equals("instructor")) {
				InstructorType instructorType = instructorTypeRepository.findById(7L).orElse(null);
				userRole.setInstructorType(instructorType);
			}

			log.info("Creating userRole '" + userRole.getRole().getName() + "' for user '" + user.getLoginId() + "' and workgroup '" + workgroup.getName() + "'");
			userRoleRepository.save(userRole);

			List<UserRole> userRoles = user.getUserRoles();
			userRoles.add(userRole);
			user.setUserRoles(userRoles);

			userService.save(user);

			if (roleName.equals("instructionalSupport") || roleName.equals("studentMasters") || roleName.equals("studentPhd")) {
				log.info("Creating Instructional Support Staff for user '" + user.getLoginId() + "'");
				SupportStaff SupportStaff = supportStaffService.findOrCreate(
						user.getFirstName(),
						user.getLastName(),
						user.getEmail(),
						user.getLoginId());
			}

			if (roleName.equals("instructor")) {
				log.info("Creating instructor for user '" + user.getLoginId() + "'");
				Instructor instructor = instructorService.findOrCreate(
					user.getFirstName(),
					user.getLastName(),
					user.getEmail(),
					user.getLoginId(),
					userRole.getWorkgroup().getId());
			}

			return userRole;
		} else {
			log.warn("userRole could not be found or created, null data submitted.");
			return null;
		}
	}

	@Override
	public void deleteByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String roleName) {
		User user = this.userService.getOneByLoginId(loginId);
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Role role = roleService.findOneByName(roleName);

		for (UserRole userRole : this.findByLoginIdAndWorkgroup(loginId, workgroup)) {
			if ( userRole.getRole().equals(role) ) {
				List<UserRole> userRoles = user.getUserRoles();
				userRoles.remove(userRole);
				user.setUserRoles(userRoles);
				userService.save(user);

				List<UserRole> workgroupUserRoles = workgroup.getUserRoles();
				workgroupUserRoles.remove(userRole);
				workgroup.setUserRoles(workgroupUserRoles);
				workgroupService.save(workgroup);

				userRoleRepository.deleteById(userRole.getId());
				return;
			}
		}
	}

	@Override
	public boolean deleteByLoginIdAndWorkgroupId(String loginId, long workgroupId) {
		User user = userService.getOneByLoginId(loginId);
		List<String> userRolesToRemove = new ArrayList<>();

		for (UserRole userRole : user.getUserRoles()){
			if(userRole.getWorkgroup() != null && workgroupId == userRole.getWorkgroup().getId() ) {
				userRolesToRemove.add(userRole.getRole().getName());
			}
		}

		for (String roleName : userRolesToRemove) {
			this.deleteByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, roleName);
		}
		return true;
	}

	@Override
	public List<UserRole> findByWorkgroupIdAndRoleToken (Long workgroupId, String roleName) {
		Workgroup workgroup = workgroupService.findOneById(workgroupId);
		Role role = roleService.findOneByName(roleName);
		List<UserRole> userRoles = new ArrayList<UserRole>();
		List<UserRole> workgroupUserRoles = this.findByWorkgroup(workgroup);

		if(workgroup != null && role != null) {
			for (UserRole userRole : workgroupUserRoles) {
				if ( userRole.getRole().getName().equals(roleName) ) {
					userRoles.add(userRole);
				}
			}
			return userRoles;
		} else {
			log.warn("userRoles could not be found, null data submitted.");
			return null;
		}
	}

	@Override
	public List<UserRole> findByWorkgroup(Workgroup workgroup) {
		return userRoleRepository.findByWorkgroupId(workgroup.getId());
	}

	@Override
	public List<Instructor> getInstructorsByScheduleIdAndWorkgroupId(long scheduleId, long workgroupId) {
		List<Long> workgroupInstructorIds = new ArrayList<Long>();
		List<Instructor> workgroupInstructors = new ArrayList<Instructor>();

		Schedule schedule = scheduleService.findById(scheduleId);
		List<SectionGroup> sectionGroups = new ArrayList<>();

		for (Course course : schedule.getCourses()) {
			sectionGroups.addAll(course.getSectionGroups());
		}

		List<UserRole> instructorRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, "instructor");
		for (UserRole userRole: instructorRoles) {
			Instructor instructor = instructorService.getOneByLoginId(userRole.getUser().getLoginId());

			if (instructor == null) {
				// Create instructor if it does not exist
				String firstName = userRole.getUser().getFirstName();
				String lastName = userRole.getUser().getLastName();
				String email = userRole.getUser().getEmail();
				String loginId = userRole.getUser().getLoginId();

				instructor = instructorService.findOrCreate(firstName, lastName, email, loginId, workgroupId);
			}

			// Add to list of instructors if not already there.
			// Prevents getting the AJS dupes error
			if (!workgroupInstructorIds.contains(instructor.getId())) {
				workgroupInstructorIds.add(instructor.getId());
				workgroupInstructors.add(instructor);
			}
		}

		List<Instructor> instructorsFromAssignments = instructorService.findBySectionGroups(sectionGroups);
		for (Instructor slotInstructor : instructorsFromAssignments) {
			if (!workgroupInstructorIds.contains(slotInstructor.getId())) {
				workgroupInstructorIds.add(slotInstructor.getId());
				workgroupInstructors.add(slotInstructor);
			}
		}

		return workgroupInstructors;
	}

	@Override
	public List<Long> getInstructorsByWorkgroupIdAndRoleToken (long workgroupId, String roleToken) {
		List<Long> workgroupInstructorIds = new ArrayList<Long>();
		List<Instructor> workgroupInstructors = new ArrayList<Instructor>();

		List<UserRole> instructorUserRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, roleToken);

		for (UserRole userRole: instructorUserRoles) {
			Instructor instructor = instructorService.getOneByLoginId(userRole.getUser().getLoginId());

			if (instructor != null) {
				// Add to list of instructors if not already there.
				// Prevents getting the AJS dupes error
				if (!workgroupInstructorIds.contains(instructor.getId())) {
					workgroupInstructorIds.add(instructor.getId());
					workgroupInstructors.add(instructor);
				}
			} else {
				log.error("Could not find instructor entity for loginId: " + userRole.getUser().getLoginId());
			}
		}

		workgroupInstructorIds = new ArrayList<Long>();

		// Sort by last name
		Collections.sort(workgroupInstructors, new Comparator<Instructor>() {
			public int compare(Instructor instructor1,Instructor instructor2){
				if (instructor1.getLastName().compareTo(instructor2.getLastName() ) > 0 ) {
					return 1;
				}
				return -1;
			}});

		// Build the Id list
		for (Instructor instructor : workgroupInstructors) {
			if (!workgroupInstructorIds.contains(instructor.getId())) {
				workgroupInstructorIds.add(instructor.getId());
			}
		}

		return workgroupInstructorIds;
	}

	@Override
	public List<UserRole> findByLoginId(String loginId) {
		return userRoleRepository.findByUserLoginId(loginId);
		//return userRoleRepository.findByLoginId(loginId);
	}

	@Override
	public UserRole findOrAddInstructorRoleToWorkgroup(Workgroup workgroup, User user) {
		List<UserRole> userRoles = this.findByLoginIdAndWorkgroup(user.getLoginId(), workgroup);

		// Look through current roles for instructor roles
		for (UserRole userRole : userRoles) {
			if (UserRole.isInstructor(userRole) == true) {
				return userRole;
			}
		}

		// Make instructor role
		UserRole userRole = new UserRole();
		userRole.setUser(user);
		userRole.setWorkgroup(workgroup);
		Role role = roleService.findOneByName("instructor");
		userRole.setRole(role);

		this.save(userRole);

		return userRole;
	}

	@Override
	public Instructor findOrAddActiveInstructor(Workgroup workgroup, User user) {
		Instructor instructor = instructorService.findOrCreate(user.getFirstName(), user.getLastName(), user.getEmail(), user.getLoginId(), workgroup.getId());

		this.findOrAddInstructorRoleToWorkgroup(workgroup, user);

		return instructor;
	}

	/**
	 * Returns a list of SupportStaff entities that are tied to userRoles for masters/phd/instructionalSupport roles.
	 * These 3 populations will all have instructionalSupportStaff entities, and represent people
	 * an academic planner might want to assign to an instructionalSupportAssignment.
	 * @param workgroupId
	 * @return
	 */
	@Override
	public List<SupportStaff> findActiveSupportStaffByWorkgroupId(long workgroupId) {
		List<String> roleTokens = new ArrayList<>(Arrays.asList("studentMasters", "studentPhd", "instructionalSupport"));
		List<UserRole> activeUserRoles = this.findByWorkgroupIdAndRoleTokens(workgroupId, roleTokens);

		List<SupportStaff> activeSupportStaffList = new ArrayList<SupportStaff>();

		for (UserRole userRole : activeUserRoles) {
			SupportStaff supportStaff = supportStaffService.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
			activeSupportStaffList.add(supportStaff);
		}

		return activeSupportStaffList;
	}

	private List<UserRole> findByWorkgroupIdAndRoleTokens(long workgroupId, List<String> roleTokens) {

		// Get roleIds from roleTokens
		List<Role> roles = roleService.getAllRoles();
		List<Long> roleIds = new ArrayList<>();

		for (Role role : roles) {
			if (roleTokens.indexOf(role.getName()) > -1) {
				roleIds.add(role.getId());
			}
		}

		return this.userRoleRepository.findByWorkgroupIdAndRoleIdIn(workgroupId, roleIds);
	}

	@Override
	public List<SupportStaff> findActiveSupportStaffByWorkgroupIdAndRoleToken(long workgroupId, String roleToken) {
		List<UserRole> instructionalSupportStaffUserRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, roleToken);

		List<SupportStaff> activeSupportStaffList = new ArrayList<SupportStaff>();

		for (UserRole userRole : instructionalSupportStaffUserRoles) {
			SupportStaff supportStaff = supportStaffService.findOrCreate(userRole.getUser().getFirstName(), userRole.getUser().getLastName(), userRole.getUser().getEmail(), userRole.getUser().getLoginId());
			activeSupportStaffList.add(supportStaff);
		}

		return activeSupportStaffList;
	}

	@Override
	public List<SupportStaff> findActiveSupportStaffByWorkgroupIdAndPreferences(long workgroupId, List<StudentSupportPreference> studentPreferences) {
		Set<SupportStaff> supportStaff = new LinkedHashSet<>(studentPreferences.stream().map(preference -> preference.getSupportStaff()).collect(Collectors.toList()));
		supportStaff.addAll(new LinkedHashSet<>(this.findActiveSupportStaffByWorkgroupId(workgroupId)));

		return new ArrayList<>(supportStaff);
	}
}
