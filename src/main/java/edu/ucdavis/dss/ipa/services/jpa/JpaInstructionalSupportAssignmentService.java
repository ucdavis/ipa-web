package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportAssignment;
import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.InstructionalSupportAssignmentRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportAssignmentService;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import sun.tools.asm.Instruction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class JpaInstructionalSupportAssignmentService implements InstructionalSupportAssignmentService {

    @Inject InstructionalSupportAssignmentRepository instructionalSupportAssignmentRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;

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
    public InstructionalSupportAssignment create(long sectionGroupId, long instructionalSupportStaffId, String type, long appointmentPercentage) {

        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);

        InstructionalSupportAssignment instructionalSupportAssignment = new InstructionalSupportAssignment();

        instructionalSupportAssignment.setInstructionalSupportStaff(instructionalSupportStaff);
        instructionalSupportAssignment.setSectionGroup(sectionGroup);
        instructionalSupportAssignment.setAppointmentPercentage(appointmentPercentage);
        instructionalSupportAssignment.setType(type);

        return this.save(instructionalSupportAssignment);
    }

    @Override
    public List<InstructionalSupportAssignment> createMultiple(long sectionGroupId, long instructionalSupportStaffId, String type, long appointmentPercentage, long numberToCreate) {

        List<InstructionalSupportAssignment> instructionalSupportAssignments = new ArrayList<InstructionalSupportAssignment>();

        for (int i = 0; i < numberToCreate; i++) {
            InstructionalSupportAssignment instructionalSupportAssignment = this.create(sectionGroupId, instructionalSupportStaffId, type, appointmentPercentage);
            instructionalSupportAssignments.add(instructionalSupportAssignment);
        }

        return instructionalSupportAssignments;
    }

    @Override
    public InstructionalSupportAssignment assignInstructionalSupportStaff(long instructionalSupportStaffId, long instructionalSupportAssignmentId) {

        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        InstructionalSupportAssignment instructionalSupportAssignment = this.findOneById(instructionalSupportAssignmentId);

        instructionalSupportAssignment.setInstructionalSupportStaff(instructionalSupportStaff);

        return this.save(instructionalSupportAssignment);
    }

    @Override
    public InstructionalSupportAssignment unassignInstructionalSupportStaff(long instructionalSupportAssignmentId) {
        InstructionalSupportAssignment instructionalSupportAssignment = this.findOneById(instructionalSupportAssignmentId);

        instructionalSupportAssignment.setInstructionalSupportStaff(null);

        return this.save(instructionalSupportAssignment);
    }
}