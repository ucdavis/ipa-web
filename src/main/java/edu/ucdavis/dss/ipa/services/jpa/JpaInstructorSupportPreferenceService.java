package edu.ucdavis.dss.ipa.services.jpa;

        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.repositories.InstructorSupportPreferenceRepository;
        import edu.ucdavis.dss.ipa.services.*;
        import org.springframework.stereotype.Service;

        import javax.inject.Inject;
        import java.util.ArrayList;
        import java.util.List;

@Service
public class JpaInstructorSupportPreferenceService implements InstructorSupportPreferenceService {

    @Inject InstructorSupportPreferenceRepository instructorSupportPreferenceRepository;
    @Inject SectionGroupService sectionGroupService;
    @Inject SupportStaffService supportStaffService;
    @Inject InstructorService instructorService;
    @Inject ScheduleService scheduleService;

    public InstructorSupportPreference save(InstructorSupportPreference instructorSupportPreference) {
        return this.instructorSupportPreferenceRepository.save(instructorSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> preferenceIds) {
        for (int i = 0; i < preferenceIds.size(); i++) {
            long preferenceId = preferenceIds.get(i);
            if (preferenceId == 0) {
                continue;
            }

            InstructorSupportPreference preference = this.findById(preferenceId);
            preference.setPriority(i+1);
            this.save(preference);
        }

        return preferenceIds;
    }

    @Override
    public InstructorSupportPreference create(long instructionalSupportStaffId, long instructorId, long sectionGroupId) {
        SupportStaff supportStaff = supportStaffService.findOneById(instructionalSupportStaffId);
        Instructor instructor = instructorService.getOneById(instructorId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);

        InstructorSupportPreference instructorSupportPreference = new InstructorSupportPreference();
        instructorSupportPreference.setSectionGroup(sectionGroup);
        instructorSupportPreference.setSupportStaff(supportStaff);
        instructorSupportPreference.setInstructor(instructor);

        // Set priority arbitrarily to a ceiling, to ensure recalculation places it at the end.
        instructorSupportPreference.setPriority(999L);

        instructorSupportPreference = this.save(instructorSupportPreference);

        this.recalculatePriorities(sectionGroup.getId(), instructor.getId());

        return this.findById(instructorSupportPreference.getId());
    }

    @Override
    public Long delete(Long instructorSupportPreferenceId) {
        InstructorSupportPreference instructorSupportPreference = this.findById(instructorSupportPreferenceId);

        if (instructorSupportPreference == null) {
            return null;
        }

        Long sectionGroupId = instructorSupportPreference.getSectionGroup().getId();
        Long instructorId = instructorSupportPreference.getInstructor().getId();

        this.instructorSupportPreferenceRepository.deleteById(instructorSupportPreferenceId);

        this.recalculatePriorities(sectionGroupId, instructorId);

        return instructorSupportPreferenceId;
    }

    private void recalculatePriorities(Long sectionGroupId, Long instructorId) {
        List<InstructorSupportPreference> instructorPreferences = this.instructorSupportPreferenceRepository.findByInstructorIdAndSectionGroupId(instructorId, sectionGroupId);

        List<InstructorSupportPreference> processedPreferences = new ArrayList<>();

        // Assign each preference value
        for (int priority = 1; priority <= instructorPreferences.size(); priority++) {

            // Find the preference with the lowest priority (that hasn't already been processed)
            InstructorSupportPreference lowestPriorityPreference = null;

            for (InstructorSupportPreference preference : instructorPreferences) {
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

    private boolean isInArray(List<InstructorSupportPreference> preferences, long id) {
        boolean isInArray = false;
        for (InstructorSupportPreference preference: preferences) {
            if (id == preference.getId()) {
                isInArray = true;
                break;
            }
        }

        return isInArray;
    }

    @Override
    public List<InstructorSupportPreference> findByInstructorIdAndTermCode(long instructorId, String termCode) {
        List<InstructorSupportPreference> preferences = new ArrayList<>();
        List<InstructorSupportPreference> unfilteredPreferences = instructorSupportPreferenceRepository.findByInstructorId(instructorId);

        for (InstructorSupportPreference preference : unfilteredPreferences) {
            if (termCode.equals(preference.getSectionGroup().getTermCode())) {
                preferences.add(preference);
            }
        }

        return preferences;
    }

    @Override
    public List<InstructorSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<InstructorSupportPreference> preferences = new ArrayList<>();

        Schedule schedule = scheduleService.findById(scheduleId);

        for (Course course : schedule.getCourses()) {
            for (SectionGroup sectionGroup : course.getSectionGroups()) {
                if (termCode.equals(sectionGroup.getTermCode())) {
                    preferences.addAll(sectionGroup.getInstructorSupportPreferences());
                }
            }
        }

        return preferences;
    }

    private InstructorSupportPreference findById(long preferenceId) {
        return this.instructorSupportPreferenceRepository.findById(preferenceId);
    }
}
