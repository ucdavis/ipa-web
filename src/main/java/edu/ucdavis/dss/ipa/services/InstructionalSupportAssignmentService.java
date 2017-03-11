package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.SupportAssignment;

import java.util.List;

@Validated
public interface InstructionalSupportAssignmentService {

    SupportAssignment findOneById(Long instructionalSupportAssignmentId);

    void delete(Long instructionalSupportAssignmentId);

    SupportAssignment create(long sectionGroupId, String type, long appointmentPercentage);

    List<SupportAssignment> createMultiple(long sectionGroupId, String type, long appointmentPercentage, long numberToCreate);

    SupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId);

    SupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId);

    List<SupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode);

    List<SupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long id, String termCode, long supportStaffId);

    SupportAssignment save(SupportAssignment supportAssignment);
}
