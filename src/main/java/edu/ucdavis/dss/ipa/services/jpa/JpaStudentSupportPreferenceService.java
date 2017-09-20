package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.repositories.StudentSupportPreferenceRepository;
import edu.ucdavis.dss.ipa.services.*;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaStudentSupportPreferenceService implements StudentSupportPreferenceService {

    @Inject
    StudentSupportPreferenceRepository studentSupportPreferenceRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportStaffService supportStaffService;
    @Inject ScheduleService scheduleService;

    @Override
    public StudentSupportPreference save(StudentSupportPreference studentSupportPreference) {
        return this.studentSupportPreferenceRepository.save(studentSupportPreference);
    }

    public StudentSupportPreference update(StudentSupportPreference studentSupportPreference) {
        Long supportStaffId = studentSupportPreference.getSupportStaff().getId();

        this.studentSupportPreferenceRepository.save(studentSupportPreference);
        this.recalculatePriorities(supportStaffId, studentSupportPreference.getSectionGroup().getTermCode());

        return this.studentSupportPreferenceRepository.findOneById(studentSupportPreference.getId());
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
        Long supportStaffId = studentSupportPreference.getSupportStaff().getId();

        // Set priority to arbitrarily high ceiling to ensure it is placed at the end
        studentSupportPreference.setPriority(999L);

        studentSupportPreference = this.save(studentSupportPreference);

        this.recalculatePriorities(supportStaffId, studentSupportPreference.getSectionGroup().getTermCode());

        return this.findById(studentSupportPreference.getId());
    }

    @Override
    public void delete(Long studentInstructionalSupportPreferenceId) {
        StudentSupportPreference studentSupportPreference = this.findById(studentInstructionalSupportPreferenceId);

        Long supportStaffId = this.findById(studentInstructionalSupportPreferenceId).getSupportStaff().getId();

        this.studentSupportPreferenceRepository.deleteById(studentInstructionalSupportPreferenceId);
        this.recalculatePriorities(supportStaffId, studentSupportPreference.getSectionGroup().getTermCode());
    }

    @Override
    public List<StudentSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<StudentSupportPreference> preferences = new ArrayList<>();
        Schedule schedule = scheduleService.findById(scheduleId);

        for (Course course : schedule.getCourses()) {
            for (SectionGroup sectionGroup : course.getSectionGroups()) {
                if (termCode.equals(sectionGroup.getTermCode())) {
                    preferences.addAll(sectionGroup.getStudentInstructionalSupportCallPreferences());
                }
            }
        }

        return preferences;
    }

    @Override
    public List<StudentSupportPreference> findByScheduleIdAndTermCodeAndSupportStaffId(long scheduleId, String termCode, long supportStaffId) {
        List<StudentSupportPreference> allPreferences = this.findByScheduleIdAndTermCode(scheduleId, termCode);
        List<StudentSupportPreference> filteredPreferences = new ArrayList<>();

        for (StudentSupportPreference preference : allPreferences) {
            if (supportStaffId == preference.getSupportStaff().getId()) {
                filteredPreferences.add(preference);
            }
        }

        return filteredPreferences;
    }

    @Override
    public StudentSupportPreference findById(long preferenceId) {
        return studentSupportPreferenceRepository.findOneById(preferenceId);
    }

    /**
     * Ensure there are no 'gaps' in preference priority in a specific term and person.
     * Example: 2,4,5 will change to 1,2,3
     * @param supportStaffId
     * @param termCode
     */
    private void recalculatePriorities(Long supportStaffId, String termCode) {
        List<StudentSupportPreference> studentSupportPreferences = this.studentSupportPreferenceRepository.findBySupportStaffId(supportStaffId);

        List<StudentSupportPreference> preferencesInTerm = new ArrayList<StudentSupportPreference>();

        List<StudentSupportPreference> processedPreferences = new ArrayList<>();

        // Identify relevant studentSupportPreferences
        for (StudentSupportPreference preference : studentSupportPreferences) {
            if (termCode.equals(preference.getSectionGroup().getTermCode())) {
                preferencesInTerm.add(preference);
            }
        }

        // Assign each preference value
        for (int priority = 1; priority <= preferencesInTerm.size(); priority++) {

            // Find the preference with the lowest priority (that hasn't already been processed)
            StudentSupportPreference lowestPriorityPreference = null;

            for (StudentSupportPreference preference : preferencesInTerm) {
                if (termCode.equals(preference.getSectionGroup().getTermCode()) == false) {
                    continue;
                }

                if (this.isInArray(processedPreferences, preference.getId())) {
                    continue;
                }

                if (lowestPriorityPreference == null) {
                    lowestPriorityPreference = preference;
                    continue;
                }

                if (preference.getPriority() < lowestPriorityPreference.getPriority()) {
                    lowestPriorityPreference = preference;
                }
            }

            // Save the preference its new priority add it to the list of processed preferences
            lowestPriorityPreference.setPriority(priority);
            this.save(lowestPriorityPreference);
            processedPreferences.add(lowestPriorityPreference);
        }
    }

    private boolean isInArray(List<StudentSupportPreference> preferences, long id) {
        boolean isInArray = false;
        for (StudentSupportPreference preference: preferences) {
            if (id == preference.getId()) {
                isInArray = true;
                break;
            }
        }

        return isInArray;
    }
}
