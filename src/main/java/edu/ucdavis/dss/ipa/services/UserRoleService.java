package edu.ucdavis.dss.ipa.services;


import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
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

	List<Instructor> getInstructorsByScheduleIdAndWorkgroupId(long scheduleId, long workgroupId);

	List<Long> getInstructorsByWorkgroupIdAndRoleToken(long workgropuId, String roleToken);

	List<UserRole> findByLoginId(String loginId);

	/**
	 * If the user does not already have an 'instructor' role, they will be assiged the 'instructor' role and type
	 * @param workgroup
	 * @param user
	 */
	UserRole findOrAddInstructorRoleToWorkgroup(Workgroup workgroup, User user);

	Instructor findOrAddActiveInstructor(Workgroup workgroup, User user);

	List<SupportStaff> findActiveSupportStaffByWorkgroupId(long workgroupId);

	List<SupportStaff> findActiveSupportStaffByWorkgroupIdAndRoleToken(long workgroupId, String studentMasters);

	List<SupportStaff> findActiveSupportStaffByWorkgroupIdAndPreferences(long workgroupId, List<StudentSupportPreference> studentPreferences);
}
