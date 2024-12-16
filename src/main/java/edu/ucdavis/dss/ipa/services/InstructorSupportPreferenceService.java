package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;

import java.util.List;

public interface InstructorSupportPreferenceService {

    /**
     * Will set the priority values of all preferences tied to the relevant instructionalSupportStaff and instructionalSupportCall based on the order of the ids.
     * @param instructorInstructionalSupportPreferenceIds
     * @param sectionGroupId
     * @return
     */
    List<Long> updatePriorities(List<Long> instructorInstructionalSupportPreferenceIds, long sectionGroupId);

    InstructorSupportPreference create (long instructionalSupportStaffId, long instructorId, long sectionGroupId, String appointmentType);

    Long delete(Long studentInstructionalSupportPreferenceId);

    List<InstructorSupportPreference> findByInstructorIdAndTermCode(long instructorId, String termCode);

    List<InstructorSupportPreference> findByScheduleIdAndTermCode(long id, String termCode);
}
