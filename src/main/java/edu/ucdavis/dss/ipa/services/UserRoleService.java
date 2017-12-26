package edu.ucdavis.dss.ipa.services;


import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.User;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface UserRoleService {

	UserRole save(@NotNull @Valid UserRole userRole);

	UserRole getOneById(Long id);
	
	List<UserRole> findByLoginIdAndWorkgroup(String loginId, Workgroup workgroup);

	List<UserRole> findByWorkgroup(Workgroup workgroup);

	List<UserRole> findByWorkgroupIdAndRoleToken(Long workgroupId, String role);

	UserRole findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String roleName);

	void deleteByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String role);

	boolean deleteByLoginIdAndWorkgroupId(String loginId, long workgroupId);

	List<Instructor> getInstructorsByWorkgroupId(long workgroupId);

	List<Long> getInstructorsByWorkgroupIdAndRoleToken(long workgropuId, String roleToken);

	List<UserRole> findByLoginId(String loginId);

	/**
	 * If the user does not already have an 'instructor' type role (senate/fed/lect), they will be assiged a senate role
	 * @param workgroup
	 * @param user
	 */
	UserRole findOrAddInstructorRoleToWorkgroup(Workgroup workgroup, User user);

	Instructor findOrAddActiveInstructor(Workgroup workgroup, User user);
}
