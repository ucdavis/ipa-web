package edu.ucdavis.dss.ipa.services.jpa;

import java.util.*;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.UserRoleRepository;

@Service
public class JpaUserRoleService implements UserRoleService {
	private static final Logger log = LogManager.getLogger();
	
	@Inject UserRoleRepository userRoleRepository;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject RoleService roleService;
	@Inject InstructorService instructorService;
	@Inject
	SupportStaffService supportStaffService;

	@Override
	@Transactional
	public UserRole save(UserRole userRole) {
		return this.userRoleRepository.save(userRole);
	}

	@Override
	public UserRole getOneById(Long id) {
		return this.userRoleRepository.findOne(id);
	}

	@Override
	public List<UserRole> findByLoginIdAndWorkgroup(String loginId, Workgroup workgroup) {
		User user = userService.getOneByLoginId(loginId);
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
		List<String> EXCLUSIVE_ROLES = Arrays.asList("senateInstructor", "federationInstructor");

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
			log.info("Creating userRole '" + userRole.getRole().getName() + "' for user '" + user.getLoginId() + "' and workgroup '" + workgroup.getName() + "'");
			userRoleRepository.save(userRole);
	
			List<UserRole> userRoles = user.getUserRoles();

			// Remove other exclusive roles if the role being added amongst them
			if (EXCLUSIVE_ROLES.contains(roleName)) {
				List<UserRole> rolesToBeRemoved = new ArrayList<UserRole>();
				for (String exclusiveRole: EXCLUSIVE_ROLES) {
					for (UserRole ur: this.findByLoginIdAndWorkgroup(loginId, workgroup)) {
						if (ur.getRole().getName().equals(exclusiveRole)) {
							rolesToBeRemoved.add(ur);
						}
					}
				}
				for (UserRole ur: rolesToBeRemoved) {
					this.deleteByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, ur.getRole().getName());
				}
			}

			userRoles.add(userRole);

			user.setUserRoles(userRoles);
			userService.save(user);

			if (roleName.equals("instructionalSupport")) {
				log.info("Creating Instructional Support Staff for user '" + user.getLoginId() + "'");
				SupportStaff SupportStaff = supportStaffService.findOrCreate(
						user.getFirstName(),
						user.getLastName(),
						user.getEmail(),
						user.getLoginId());
			}

			if (roleName.equals("senateInstructor") || roleName.equals("federationInstructor") || roleName.equals("lecturer")) {
				log.info("Creating instructor for user '" + user.getLoginId() + "'");
				Instructor instructor = instructorService.findOrCreate(
					user.getFirstName(),
					user.getLastName(),
					user.getEmail(),
					user.getLoginId(),
					userRole.getWorkgroup().getId());

				// Attempt to get employeeId from DW
//				DwClient dwClient = null;
//				List<DwInstructor> dwInstructors = new ArrayList<DwInstructor>();
//
//				try {
//					dwClient = new DwClient();
//					dwInstructors = dwClient.searchInstructors(user.getLastName());
//
//					for (DwInstructor dwInstructor : dwInstructors) {
//
//						if (dwInstructor.getLoginId() != null &&
//							dwInstructor.getEmployeeId() != null &&
//							dwInstructor.getLoginId().equals(user.getLoginId())	) {
//
//							instructor.setUcdStudentSID(dwInstructor.getEmployeeId());
//							log.info("Queried Data Warehouse for employeeId on instructor '" + instructor.getLoginId() + "'");
//							instructorService.save(instructor);
//						}
//					}
//				} catch (Exception e) {
//					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
//				}
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

				userRoleRepository.delete(userRole);

				if(roleName.equals("senateInstructor") || roleName.equals("federationInstructor") ) {
					instructorService.removeOrphanedByLoginId(loginId);
				}
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
	public List<Instructor> getInstructorsByWorkgroupId(long workgroupId) {
		String[] INSTRUCTOR_ROLES = {"federationInstructor", "senateInstructor", "lecturer"};

		List<Instructor> workgroupInstructors = new ArrayList<Instructor>();

		for (String instructorRole: INSTRUCTOR_ROLES) {
			List<UserRole> instructorRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, instructorRole);
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

				// Add to list of instructors if not already there. This should never happen since
				// an instructor should be either Senate OR Federation, but not both.
				// Prevents getting the AJS dupes error
				if (!workgroupInstructors.contains(instructor)) {
					workgroupInstructors.add(instructor);
				}
			}
		}

		return workgroupInstructors;
	}

	@Override
	public List<Long> getInstructorsByWorkgroupIdAndRoleToken(long workgroupId, String roleToken) {
		String[] INSTRUCTOR_ROLES = {roleToken};

		List<Long> workgroupInstructorIds = new ArrayList<Long>();
		List<Instructor> workgroupInstructors = new ArrayList<Instructor>();

		for (String instructorRole: INSTRUCTOR_ROLES) {
			List<UserRole> instructorRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, instructorRole);
			for (UserRole userRole: instructorRoles) {
				Instructor instructor = instructorService.getOneByLoginId(userRole.getUser().getLoginId());
				if (instructor != null) {
					// Add to list of instructors if not already there. This should never happen since
					// an instructor should be either Senate OR Federation, but not both.
					// Prevents getting the AJS dupes error
					if (!workgroupInstructorIds.contains(instructor.getId())) {
						workgroupInstructorIds.add(instructor.getId());
						workgroupInstructors.add(instructor);
					}
				} else {
					Exception e = new Exception("Could not find instructor entity for loginId: " + userRole.getUser().getLoginId());
					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
				}
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
		Role role = roleService.findOneByName("senateInstructor");
		userRole.setRole(role);

		this.save(userRole);

		return userRole;
	}

}

