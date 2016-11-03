package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportPreference;

import java.util.List;

public interface StudentInstructionalSupportPreferenceService {

    /**
     * Will set the priority values of all preferences tied to the relevant instructionalSupportStaff and instructionalSupportCall based on the order of the ids.
     * @param studentInstructionalSupportPreferenceIds
     * @return
     */
    List<Long> updatePriorities(List<Long> studentInstructionalSupportPreferenceIds);

    StudentInstructionalSupportPreference create (long instructionalSupportStaffId, long sectionGroupId, String type, String comment);

    void delete(Long studentInstructionalSupportPreferenceId);
}
