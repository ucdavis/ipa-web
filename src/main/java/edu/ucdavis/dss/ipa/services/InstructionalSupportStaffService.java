package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.UserRole;

import java.util.List;

public interface InstructionalSupportStaffService {
    InstructionalSupportStaff findOneById(long instructionalSupportStaffId);

    InstructionalSupportStaff findOrCreate(String firstName, String lastName, String email, String loginId);

    List<InstructionalSupportStaff> findActiveByWorkgroupId(long workgroupId);

    List<InstructionalSupportStaff> findActiveByWorkgroupIdAndRoleToken(long workgroupId, String studentMasters);

    InstructionalSupportStaff findByLoginId(String loginId);
}
