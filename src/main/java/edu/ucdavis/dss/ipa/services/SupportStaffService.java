package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;

import java.util.List;

public interface SupportStaffService {
    SupportStaff findOneById(long instructionalSupportStaffId);

    SupportStaff findOrCreate(String firstName, String lastName, String email, String loginId);

    SupportStaff findByLoginId(String loginId);

    List<SupportStaff> findByScheduleId(long id);

    List<SupportStaff> findBySupportAssignments(List<SupportAssignment> supportAssignments);
}
