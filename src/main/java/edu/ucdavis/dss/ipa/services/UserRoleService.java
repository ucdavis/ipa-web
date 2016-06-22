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

	UserRole save(@NotNull @Valid UserRole userRole);

	UserRole getOneById(Long id);
	
	List<UserRole> findByLoginIdAndWorkgroup(String loginId, Workgroup workgroup);

	List<UserRole> findByWorkgroup(Workgroup workgroup);

	List<UserRole> findByWorkgroupIdAndRoleToken(Long workgroupId, String role);

	UserRole findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String roleName);

	void deleteByLoginIdAndWorkgroupIdAndRoleToken(String loginId, Long workgroupId, String role);

	boolean deleteByLoginIdAndWorkgroupId(String loginId, Long workgroupId);

	List<Instructor> getInstructorsByWorkgroupId(long workgroupId);

	List<UserRole> findByLoginId(String loginId);
}
