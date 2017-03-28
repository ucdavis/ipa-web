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

        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        instructorSupportPreference.setPriority(1L);

        return this.save(instructorSupportPreference);
    }

    @Override
    public void delete(Long instructorInstructionalSupportPreferenceId) {
        this.instructorSupportPreferenceRepository.deleteById(instructorInstructionalSupportPreferenceId);
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
