package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportStaff;

import java.util.List;

public interface SupportStaffService {
    SupportStaff findOneById(long instructionalSupportStaffId);

    SupportStaff findOrCreate(String firstName, String lastName, String email, String loginId);

    List<SupportStaff> findActiveByWorkgroupId(long workgroupId);

    List<SupportStaff> findActiveByWorkgroupIdAndRoleToken(long workgroupId, String studentMasters);

    SupportStaff findByLoginId(String loginId);

    List<SupportStaff> findByScheduleId(long id);
}
