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

	UserRole findOrCreateByLoginIdAndWorkgroupCodeAndRoleToken(String loginId, String workgroupCode, String role);

	List<UserRole> findByWorkgroupIdAndRoleToken(Long workgroupId, String role);

	void deleteByLoginIdAndWorkgroupCodeAndRoleToken(String loginId, String workgroupCode, String role);

	boolean deleteByLoginIdAndWorkgroupCode(String loginId, String workgroupCode);

	List<Instructor> getInstructorsByWorkgroupId(long workgroupId);

}
