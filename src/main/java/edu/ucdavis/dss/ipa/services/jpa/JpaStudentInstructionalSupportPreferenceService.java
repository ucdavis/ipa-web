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


    public StudentSupportPreference save(StudentSupportPreference studentSupportPreference) {
        return this.studentInstructionalSupportPreferenceRepository.save(studentSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> preferenceIds) {
        for (int i = 0; i < preferenceIds.size(); i++) {
            long preferenceId = preferenceIds.get(i);

            StudentSupportPreference preference = this.findById(preferenceId);
            preference.setPriority(i+1);
            this.save(preference);
        }

        return preferenceIds;
    }

    @Override
    public StudentSupportPreference create(long instructionalSupportStaffId, long supportCallId, long sectionGroupId, String type, String comment) {
        SupportStaff supportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        StudentSupportCall studentSupportCall = studentInstructionalSupportCallService.findOneById(supportCallId);

        StudentSupportPreference studentSupportPreference = new StudentSupportPreference();
        studentSupportPreference.setSectionGroup(sectionGroup);
        studentSupportPreference.setSupportStaff(supportStaff);
        studentSupportPreference.setType(type);
        studentSupportPreference.setComment(comment);
        studentSupportPreference.setStudentSupportCall(studentSupportCall);
        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        studentSupportPreference.setPriority(1L);

        return this.save(studentSupportPreference);
    }

    @Override
    public void delete(Long studentInstructionalSupportPreferenceId) {
        this.studentInstructionalSupportPreferenceRepository.deleteById(studentInstructionalSupportPreferenceId);
    }

    @Override
    public List<StudentSupportPreference> findBySupportStaffIdAndStudentSupportCallId(long supportStaffId, long studentSupportCallId) {
        return this.studentInstructionalSupportPreferenceRepository.findByInstructionalSupportStaffIdAndStudentInstructionalSupportCallId(supportStaffId, studentSupportCallId);
    }

    @Override
    public List<StudentSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentSupportCall> supportCalls = studentInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<StudentSupportPreference> preferences = new ArrayList<>();

        for (StudentSupportCall supportCall : supportCalls) {
            if (supportCall.getTermCode().equals(termCode)) {
                preferences.addAll(supportCall.getStudentSupportPreferences());
            }
        }

        return preferences;
    }

    private StudentSupportPreference findById(long preferenceId) {
        return studentInstructionalSupportPreferenceRepository.findOneById(preferenceId);
    }
}
