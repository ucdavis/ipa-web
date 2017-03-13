package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;

import java.util.List;

public interface InstructorSupportPreferenceService {

    /**
     * Will set the priority values of all preferences tied to the relevant instructionalSupportStaff and instructionalSupportCall based on the order of the ids.
     * @param instructorInstructionalSupportPreferenceIds
     * @return
     */
    List<Long> updatePriorities(List<Long> instructorInstructionalSupportPreferenceIds);

    InstructorSupportPreference create (long instructionalSupportStaffId, long instructorId, long supportCallId, long sectionGroupId);

    void delete(Long studentInstructionalSupportPreferenceId);

    List<InstructorSupportPreference> findByInstructorIdAndInstructorSupportCallId(long instructorId, long instructorSupportCallId);

    List<InstructorSupportPreference> findByScheduleIdAndTermCode(long id, String termCode);
}
