package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import edu.ucdavis.dss.ipa.entities.SupportStaff;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.InstructionalSupportAssignmentRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportAssignmentService;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collection;

@Service
public class JpaInstructionalSupportAssignmentService implements InstructionalSupportAssignmentService {

    @Inject InstructionalSupportAssignmentRepository instructionalSupportAssignmentRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject ScheduleService scheduleService;

    @Override
    public SupportAssignment save(SupportAssignment supportAssignment) {
        return this.instructionalSupportAssignmentRepository.save(supportAssignment);
    }

    @Override
    public SupportAssignment findOneById(Long instructionalSupportAssignmentId) {
        return this.instructionalSupportAssignmentRepository.findById(instructionalSupportAssignmentId);
    }

    @Override
    public void delete(Long instructionalSupportAssignmentId) {
        this.instructionalSupportAssignmentRepository.deleteById(instructionalSupportAssignmentId);
    }

    @Override
    public SupportAssignment create(long sectionGroupId, String type, long appointmentPercentage) {

        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        SupportAssignment supportAssignment = new SupportAssignment();

        supportAssignment.setSectionGroup(sectionGroup);
        supportAssignment.setAppointmentPercentage(appointmentPercentage);
        supportAssignment.setAppointmentType(type);

        return this.save(supportAssignment);
    }

    @Override
    public List<SupportAssignment> createMultiple(long sectionGroupId, String type, long appointmentPercentage, long numberToCreate) {

        List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();

        for (int i = 0; i < numberToCreate; i++) {
            SupportAssignment supportAssignment = this.create(sectionGroupId, type, appointmentPercentage);
            supportAssignments.add(supportAssignment);
        }

        return supportAssignments;
    }

    @Override
    public SupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId) {

        SupportStaff supportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        SupportAssignment supportAssignment = this.findOneById(instructionalSupportAssignmentId);

        supportAssignment.setSupportStaff(supportStaff);

        return this.save(supportAssignment);
    }

    @Override
    public SupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId) {
        SupportAssignment supportAssignment = this.findOneById(instructionalSupportAssignmentId);

        supportAssignment.setSupportStaff(null);

        return this.save(supportAssignment);
    }

    @Override
    public List<SupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        Schedule schedule = this.scheduleService.findById(scheduleId);
        List<SupportAssignment> supportAssignments = new ArrayList<SupportAssignment>();

        // Find all sectionGroups for the specified schedule and termCode
        List<SectionGroup> sectionGroups = schedule.getCourses()
                .stream()
                .map(course -> course.getSectionGroups().stream()
                        .filter(sectionGroup -> termCode == null || termCode.trim().isEmpty() || sectionGroup.getTermCode().equals(termCode.trim()))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Find all supportAssignments associated to the sectionGroups.
        for (SectionGroup sectionGroup : sectionGroups) {
            supportAssignments.addAll(sectionGroup.getSupportAssignments());
        }

        return supportAssignments;
    }

    /**
     * Returns support assignments assigned to the specified support staff.
     * @param scheduleId
     * @param termCode
     * @param supportStaffId
     * @return
     */
    @Override
    public List<SupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long scheduleId, String termCode, long supportStaffId) {
        List<SupportAssignment> allSupportAssignments = this.findByScheduleIdAndTermCode(scheduleId, termCode);
        List<SupportAssignment> supportStaffAssignments = new ArrayList<>();

        for (SupportAssignment slotAssignment : allSupportAssignments) {
            if (slotAssignment.getSupportStaff() != null
            && slotAssignment.getSupportStaff().getId() == supportStaffId) {
                supportStaffAssignments.add(slotAssignment);
            }
        }

        return supportStaffAssignments;
    }
}