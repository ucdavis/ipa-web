package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentSupportPreference;

import java.util.List;

public interface StudentSupportPreferenceService {

    /**
     * Will set the priority values of all preferences tied to the relevant instructionalSupportStaff and instructionalSupportCall based on the order of the ids.
     * @param studentInstructionalSupportPreferenceIds
     * @return
     */
    List<Long> updatePriorities(List<Long> studentInstructionalSupportPreferenceIds);

    StudentSupportPreference create (StudentSupportPreference studentSupportPreferenceDTO);

    void delete(Long studentInstructionalSupportPreferenceId);

    List<StudentSupportPreference> findByScheduleIdAndTermCode(long id, String termCode);

    List<StudentSupportPreference> findByScheduleIdAndTermCodeAndSupportStaffId(long id, String termCode, long supportStaffId);
}
