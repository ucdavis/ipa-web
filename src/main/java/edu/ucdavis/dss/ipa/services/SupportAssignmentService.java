package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.SupportAssignment;

import java.util.List;

@Validated
public interface SupportAssignmentService {

    SupportAssignment findOneById(Long instructionalSupportAssignmentId);

    void delete(Long instructionalSupportAssignmentId);

    SupportAssignment create(SectionGroup sectionGroup, String type, long appointmentPercentage);

    List<SupportAssignment> createMultiple(SectionGroup sectionGroup, String type, long appointmentPercentage, long numberToCreate);

    SupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId);

    SupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId);

    List<SupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode);

    List<SupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long id, String termCode, long supportStaffId);

    SupportAssignment save(SupportAssignment supportAssignment);

    List<SupportAssignment> findBySectionGroups(List<SectionGroup> sectionGroups);

    List<SupportAssignment> findVisibleByScheduleAndInstructorId(Schedule schedule, long instructorId);
}
