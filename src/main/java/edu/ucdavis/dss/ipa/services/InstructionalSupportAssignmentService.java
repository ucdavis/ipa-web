package edu.ucdavis.dss.ipa.services;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportAssignment;

import java.util.List;

@Validated
public interface InstructionalSupportAssignmentService {

    InstructionalSupportAssignment findOneById(Long instructionalSupportAssignmentId);

    void delete(Long instructionalSupportAssignmentId);

    InstructionalSupportAssignment create(long sectionGroupId, String type, long appointmentPercentage);

    List<InstructionalSupportAssignment> createMultiple(long sectionGroupId, String type, long appointmentPercentage, long numberToCreate);

    InstructionalSupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId);

    InstructionalSupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId);

    List<InstructionalSupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode);

    List<InstructionalSupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long id, String termCode, long supportStaffId);
}
