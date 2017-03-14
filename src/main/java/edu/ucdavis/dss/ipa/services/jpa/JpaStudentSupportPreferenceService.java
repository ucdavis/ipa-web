package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.StudentSupportPreferenceRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentSupportPreferenceService implements StudentSupportPreferenceService {

    @Inject
    StudentSupportPreferenceRepository studentSupportPreferenceRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject
    SupportStaffService supportStaffService;

    public StudentSupportPreference save(StudentSupportPreference studentSupportPreference) {
        return this.studentSupportPreferenceRepository.save(studentSupportPreference);
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
    public StudentSupportPreference create(StudentSupportPreference studentSupportPreference) {
        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        studentSupportPreference.setPriority(1L);

        return this.save(studentSupportPreference);
    }

    @Override
    public void delete(Long studentInstructionalSupportPreferenceId) {
        this.studentSupportPreferenceRepository.deleteById(studentInstructionalSupportPreferenceId);
    }

    @Override
    public List<StudentSupportPreference> findBySupportStaffIdAndStudentSupportCallId(long supportStaffId, long studentSupportCallId) {
        return this.studentSupportPreferenceRepository.findBySupportStaffIdAndStudentSupportCallId(supportStaffId, studentSupportCallId);
    }

    @Override
    public List<StudentSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentSupportPreference> preferences = studentSupportPreferenceRepository.findByScheduleId(scheduleId);

        for (StudentSupportPreference preference : preferences) {
            if (preference.getSectionGroup().getTermCode().equals(termCode)) {
                preferences.add(preference);
            }
        }

        return preferences;
    }

    private StudentSupportPreference findById(long preferenceId) {
        return studentSupportPreferenceRepository.findOneById(preferenceId);
    }
}
