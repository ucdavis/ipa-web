package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportPreference;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportPreferenceRepository;
import edu.ucdavis.dss.ipa.services.InstructionalSupportStaffService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import edu.ucdavis.dss.ipa.services.StudentInstructionalSupportPreferenceService;

import javax.inject.Inject;
import java.util.List;

public class JpaStudentInstructionalSupportPreferenceService implements StudentInstructionalSupportPreferenceService {

    @Inject StudentInstructionalSupportPreferenceRepository studentInstructionalSupportPreferenceRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;

    public StudentInstructionalSupportPreference save(StudentInstructionalSupportPreference studentInstructionalSupportPreference) {
        return this.studentInstructionalSupportPreferenceRepository.save(studentInstructionalSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> studentInstructionalSupportPreferenceIds) {
        return null;
    }

    @Override
    public StudentInstructionalSupportPreference create(long instructionalSupportStaffId, long sectionGroupId, String type, String comment) {
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        StudentInstructionalSupportPreference studentInstructionalSupportPreference = new StudentInstructionalSupportPreference();
        studentInstructionalSupportPreference.setSectionGroup(sectionGroup);
        studentInstructionalSupportPreference.setInstructionalSupportStaff(instructionalSupportStaff);
        studentInstructionalSupportPreference.setType(type);
        studentInstructionalSupportPreference.setComment(comment);

        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        studentInstructionalSupportPreference.setPriority(1L);

        return this.save(studentInstructionalSupportPreference);
    }

    @Override
    public void delete(Long studentInstructionalSupportPreferenceId) {
        this.studentInstructionalSupportPreferenceRepository.deleteById(studentInstructionalSupportPreferenceId);
    }
}
