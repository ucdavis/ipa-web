package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportAssignment;
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
    public InstructionalSupportAssignment save(InstructionalSupportAssignment instructionalSupportAssignment) {
        return this.instructionalSupportAssignmentRepository.save(instructionalSupportAssignment);
    }

    @Override
    public InstructionalSupportAssignment findOneById(Long instructionalSupportAssignmentId) {
        return this.instructionalSupportAssignmentRepository.findById(instructionalSupportAssignmentId);
    }

    @Override
    public void delete(Long instructionalSupportAssignmentId) {
        this.instructionalSupportAssignmentRepository.deleteById(instructionalSupportAssignmentId);
    }

    @Override
    public InstructionalSupportAssignment create(long sectionGroupId, String type, long appointmentPercentage) {

        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        InstructionalSupportAssignment instructionalSupportAssignment = new InstructionalSupportAssignment();

        instructionalSupportAssignment.setSectionGroup(sectionGroup);
        instructionalSupportAssignment.setAppointmentPercentage(appointmentPercentage);
        instructionalSupportAssignment.setAppointmentType(type);

        return this.save(instructionalSupportAssignment);
    }

    @Override
    public List<InstructionalSupportAssignment> createMultiple(long sectionGroupId, String type, long appointmentPercentage, long numberToCreate) {

        List<InstructionalSupportAssignment> instructionalSupportAssignments = new ArrayList<InstructionalSupportAssignment>();

        for (int i = 0; i < numberToCreate; i++) {
            InstructionalSupportAssignment instructionalSupportAssignment = this.create(sectionGroupId, type, appointmentPercentage);
            instructionalSupportAssignments.add(instructionalSupportAssignment);
        }

        return instructionalSupportAssignments;
    }

    @Override
    public InstructionalSupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId) {

        SupportStaff supportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        InstructionalSupportAssignment instructionalSupportAssignment = this.findOneById(instructionalSupportAssignmentId);

        instructionalSupportAssignment.setSupportStaff(supportStaff);

        return this.save(instructionalSupportAssignment);
    }

    @Override
    public InstructionalSupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId) {
        InstructionalSupportAssignment instructionalSupportAssignment = this.findOneById(instructionalSupportAssignmentId);

        instructionalSupportAssignment.setSupportStaff(null);

        return this.save(instructionalSupportAssignment);
    }

    @Override
    public List<InstructionalSupportAssignment> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        Schedule schedule = this.scheduleService.findById(scheduleId);
        List<InstructionalSupportAssignment> instructionalSupportAssignments = new ArrayList<InstructionalSupportAssignment>();

        // Find all sectionGroups for the specified schedule and termCode
        List<SectionGroup> sectionGroups = schedule.getCourses()
                .stream()
                .map(course -> course.getSectionGroups().stream()
                        .filter(sectionGroup -> termCode == null || termCode.trim().isEmpty() || sectionGroup.getTermCode().equals(termCode.trim()))
                        .collect(Collectors.toList()))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        // Find all instructionalSupportAssignments associated to the sectionGroups.
        for (SectionGroup sectionGroup : sectionGroups) {
            instructionalSupportAssignments.addAll(sectionGroup.getInstructionalSupportAssignments());
        }

        return instructionalSupportAssignments;
    }

    /**
     * Returns support assignments assigned to the specified support staff.
     * @param scheduleId
     * @param termCode
     * @param supportStaffId
     * @return
     */
    @Override
    public List<InstructionalSupportAssignment> findByScheduleIdAndTermCodeAndSupportStaffId(long scheduleId, String termCode, long supportStaffId) {
        List<InstructionalSupportAssignment> allSupportAssignments = this.findByScheduleIdAndTermCode(scheduleId, termCode);
        List<InstructionalSupportAssignment> supportStaffAssignments = new ArrayList<>();

        for (InstructionalSupportAssignment slotAssignment : allSupportAssignments) {
            if (slotAssignment.getSupportStaff() != null
            && slotAssignment.getSupportStaff().getId() == supportStaffId) {
                supportStaffAssignments.add(slotAssignment);
            }
        }

        return supportStaffAssignments;
    }
}