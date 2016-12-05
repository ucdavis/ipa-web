package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.StudentInstructionalSupportPreferenceRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentInstructionalSupportPreferenceService implements StudentInstructionalSupportPreferenceService {

    @Inject StudentInstructionalSupportPreferenceRepository studentInstructionalSupportPreferenceRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject StudentInstructionalSupportCallService studentInstructionalSupportCallService;


    public StudentInstructionalSupportPreference save(StudentInstructionalSupportPreference studentInstructionalSupportPreference) {
        return this.studentInstructionalSupportPreferenceRepository.save(studentInstructionalSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> studentInstructionalSupportPreferenceIds) {
        return null;
    }

    @Override
    public StudentInstructionalSupportPreference create(long instructionalSupportStaffId, long supportCallId, long sectionGroupId, String type, String comment) {
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        StudentInstructionalSupportCall studentInstructionalSupportCall = studentInstructionalSupportCallService.findOneById(supportCallId);

        StudentInstructionalSupportPreference studentInstructionalSupportPreference = new StudentInstructionalSupportPreference();
        studentInstructionalSupportPreference.setSectionGroup(sectionGroup);
        studentInstructionalSupportPreference.setInstructionalSupportStaff(instructionalSupportStaff);
        studentInstructionalSupportPreference.setType(type);
        studentInstructionalSupportPreference.setComment(comment);
        studentInstructionalSupportPreference.setStudentInstructionalSupportCall(studentInstructionalSupportCall);
        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        studentInstructionalSupportPreference.setPriority(1L);

        return this.save(studentInstructionalSupportPreference);
    }

    @Override
    public void delete(Long studentInstructionalSupportPreferenceId) {
        this.studentInstructionalSupportPreferenceRepository.deleteById(studentInstructionalSupportPreferenceId);
    }

    @Override
    public List<StudentInstructionalSupportPreference> findBySupportStaffIdAndStudentSupportCallId(long supportStaffId, long studentSupportCallId) {
        return this.studentInstructionalSupportPreferenceRepository.findByInstructionalSupportStaffIdAndStudentInstructionalSupportCallId(supportStaffId, studentSupportCallId);
    }

    @Override
    public List<StudentInstructionalSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentInstructionalSupportCall> supportCalls = studentInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<StudentInstructionalSupportPreference> preferences = new ArrayList<>();

        for (StudentInstructionalSupportCall supportCall : supportCalls) {
            if (supportCall.getTermCode().equals(termCode)) {
                preferences.addAll(supportCall.getStudentInstructionalSupportPreferences());
            }
        }

        return preferences;
    }
}
