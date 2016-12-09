package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportPreference;

import java.util.List;

public interface InstructorInstructionalSupportPreferenceService {

    /**
     * Will set the priority values of all preferences tied to the relevant instructionalSupportStaff and instructionalSupportCall based on the order of the ids.
     * @param instructorInstructionalSupportPreferenceIds
     * @return
     */
    List<Long> updatePriorities(List<Long> instructorInstructionalSupportPreferenceIds);

    InstructorInstructionalSupportPreference create (long instructionalSupportStaffId, long instructorId, long supportCallId, long sectionGroupId);

    void delete(Long studentInstructionalSupportPreferenceId);

    List<InstructorInstructionalSupportPreference> findByInstructorIdAndInstructorSupportCallId(long instructorId, long instructorSupportCallId);

    List<InstructorInstructionalSupportPreference> findByScheduleIdAndTermCode(long id, String termCode);
}
