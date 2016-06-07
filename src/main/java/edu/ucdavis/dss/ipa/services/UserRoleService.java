package edu.ucdavis.dss.ipa.services;


import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface UserRoleService {

	UserRole saveUserRole(@NotNull @Valid UserRole userRole);

	UserRole findOneById(Long id);
	
	List<UserRole> findByUserAndWorkgroup(String loginId, Workgroup workgroup);

	List<UserRole> findByWorkgroup(Workgroup workgroup);

	UserRole findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String role);

	List<UserRole> findByWorkgroupIdAndRoleToken(Long workgroupId, String role);

	void deleteByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String role);

	void deleteUserRoleById(Long id);

	boolean deleteByLoginIdAndWorkgroupId(String loginId, Long workgroupId);

	List<Instructor> getInstructorsByWorkgroupId(long workgroupId);

}
