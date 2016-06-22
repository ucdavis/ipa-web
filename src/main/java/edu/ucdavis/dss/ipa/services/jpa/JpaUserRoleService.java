package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.UserRoleRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaUserRoleService implements UserRoleService {
	private static final Logger log = LogManager.getLogger();
	
	@Inject UserRoleRepository userRoleRepository;
	@Inject UserService userService;
	@Inject WorkgroupService workgroupService;
	@Inject RoleService roleService;
	@Inject InstructorService instructorService;

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
	public UserRole findOrCreateByLoginIdAndWorkgroupCodeAndRoleToken(String loginId, String workgroupCode, String roleName) {
		List<String> EXCLUSIVE_ROLES = Arrays.asList("senateInstructor", "federationInstructor");

		User user = this.userService.findOrCreateByLoginId(loginId);
		Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
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
			userRole.setActive(true);
			log.info("Creating userRole '" + userRole.getRoleToken() + "' for user '" + user.getLoginId() + "' and workgroup '" + workgroup.getName() + "'");
			userRoleRepository.save(userRole);
	
			List<UserRole> userRoles = user.getUserRoles();

			// Remove other exclusive roles if the role being added amongst them
			if (EXCLUSIVE_ROLES.contains(roleName)) {
				List<UserRole> rolesToBeRemoved = new ArrayList<UserRole>();
				for (String exclusiveRole: EXCLUSIVE_ROLES) {
					for (UserRole ur: this.findByLoginIdAndWorkgroup(loginId, workgroup)) {
						if (ur.getRoleToken().equals(exclusiveRole)) {
							rolesToBeRemoved.add(ur);
						}
					}
				}
				for (UserRole ur: rolesToBeRemoved) {
					this.deleteByLoginIdAndWorkgroupCodeAndRoleToken(loginId, workgroupCode, ur.getRoleToken());
				}
			}

			userRoles.add(userRole);

			user.setUserRoles(userRoles);
			userService.save(user);

			if(roleName.equals("senateInstructor") || roleName.equals("federationInstructor")) {
				log.info("Creating instructor for user '" + user.getLoginId() + "'");
				Instructor instructor = instructorService.findOrCreate(
					user.getFirstName(),
					user.getLastName(),
					user.getEmail(),
					user.getLoginId(),
					userRole.getWorkgroup().getId());

				// Attempt to get employeeId from DW
				DwClient dwClient = null;
				List<DwInstructor> dwInstructors = new ArrayList<DwInstructor	>();

				try {
					dwClient = new DwClient();
					dwInstructors = dwClient.searchInstructors(user.getLastName());

					for (DwInstructor dwInstructor : dwInstructors) {

						if (dwInstructor.getLoginId() != null &&
							dwInstructor.getEmployeeId() != null &&
							dwInstructor.getLoginId().equals(user.getLoginId())	) {

							instructor.setEmployeeId(dwInstructor.getEmployeeId());
							log.info("Queried Data Warehouse for employeeId on instructor '" + instructor.getLoginId() + "'");
							instructorService.save(instructor);
						}
					}
				} catch (Exception e) {
					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
				}
			}
			return userRole;
		} else {
			log.warn("userRole could not be found or created, null data submitted.");
			return null;
		}
	}

	@Override
	public void deleteByLoginIdAndWorkgroupCodeAndRoleToken(String loginId, String workgroupCode, String roleName) {
		User user = this.userService.getOneByLoginId(loginId);
		Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);
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
	public boolean deleteByLoginIdAndWorkgroupCode(String loginId, String workgroupCode) {
		User user = userService.getOneByLoginId(loginId);
		List<String> userRolesToRemove = new ArrayList<String>();

		for (UserRole userRole : user.getUserRoles()){
			if(userRole.getWorkgroup() != null && workgroupCode.equals(userRole.getWorkgroup().getCode()) ) {
				userRolesToRemove.add(userRole.getRole().getName());
			}
		}

		for (String roleName : userRolesToRemove) {
			this.deleteByLoginIdAndWorkgroupCodeAndRoleToken(loginId, workgroupCode, roleName);
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
		String[] INSTRUCTOR_ROLES = {"federationInstructor", "senateInstructor"};

		List<Instructor> workgroupInstructors = new ArrayList<Instructor>();

		for (String instructorRole: INSTRUCTOR_ROLES) {
			List<UserRole> instructorRoles = this.findByWorkgroupIdAndRoleToken(workgroupId, instructorRole);
			for (UserRole userRole: instructorRoles) {
				Instructor instructor = instructorService.getOneByLoginId(userRole.getUser().getLoginId());
				if (instructor != null) {
					// Add to list of instructors if not already there. This should never happen since
					// an instructor should be either Senate OR Federation, but not both.
					// Prevents getting the AJS dupes error
					if (!workgroupInstructors.contains(instructor)) {
						workgroupInstructors.add(instructor);
					}
				} else {
					Exception e = new Exception("Could not find instructor entity for loginId: " + userRole.getUser().getLoginId());
					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
				}
			}
		}

		return workgroupInstructors;
	}

	@Override
	public List<UserRole> findByLoginId(String loginId) {
		return userRoleRepository.findByLoginId(loginId);
	}

}

