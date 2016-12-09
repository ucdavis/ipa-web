package edu.ucdavis.dss.ipa.services.jpa;

        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.repositories.InstructorInstructionalSupportPreferenceRepository;
        import edu.ucdavis.dss.ipa.services.*;
        import org.springframework.stereotype.Service;

        import javax.inject.Inject;
        import java.util.ArrayList;
        import java.util.List;

@Service
public class JpaInstructorInstructionalSupportPreferenceService implements InstructorInstructionalSupportPreferenceService {

    @Inject
    InstructorInstructionalSupportPreferenceRepository instructorInstructionalSupportPreferenceRepository;
    @Inject
    SectionGroupService sectionGroupService;
    @Inject
    InstructionalSupportStaffService instructionalSupportStaffService;
    @Inject
    InstructorInstructionalSupportCallService instructorInstructionalSupportCallService;
    @Inject InstructorService instructorService;

    public InstructorInstructionalSupportPreference save(InstructorInstructionalSupportPreference instructorInstructionalSupportPreference) {
        return this.instructorInstructionalSupportPreferenceRepository.save(instructorInstructionalSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> instructorInstructionalSupportPreferenceIds) {
        return null;
    }

    @Override
    public InstructorInstructionalSupportPreference create(long instructionalSupportStaffId, long instructorId, long supportCallId, long sectionGroupId) {
        InstructionalSupportStaff instructionalSupportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        Instructor instructor = instructorService.getOneById(instructorId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        InstructorInstructionalSupportCall instructorInstructionalSupportCall = instructorInstructionalSupportCallService.findOneById(supportCallId);

        InstructorInstructionalSupportPreference instructorInstructionalSupportPreference = new InstructorInstructionalSupportPreference();
        instructorInstructionalSupportPreference.setSectionGroup(sectionGroup);
        instructorInstructionalSupportPreference.setInstructionalSupportStaff(instructionalSupportStaff);
        instructorInstructionalSupportPreference.setInstructor(instructor);
        instructorInstructionalSupportPreference.setInstructorInstructionalSupportCall(instructorInstructionalSupportCall);
        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        instructorInstructionalSupportPreference.setPriority(1L);

        return this.save(instructorInstructionalSupportPreference);
    }

    @Override
    public void delete(Long instructorInstructionalSupportPreferenceId) {
        this.instructorInstructionalSupportPreferenceRepository.deleteById(instructorInstructionalSupportPreferenceId);
    }

    @Override
    public List<InstructorInstructionalSupportPreference> findByInstructorIdAndInstructorSupportCallId(long instructorId, long instructorSupportCallId) {
        return this.instructorInstructionalSupportPreferenceRepository.findByInstructorIdAndInstructorInstructionalSupportCallId(instructorId, instructorSupportCallId);
    }

    @Override
    public List<InstructorInstructionalSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<InstructorInstructionalSupportCall> supportCalls = instructorInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<InstructorInstructionalSupportPreference> preferences = new ArrayList<>();

        for (InstructorInstructionalSupportCall supportCall : supportCalls) {
            if (supportCall.getTermCode().equals(termCode)) {
                preferences.addAll(supportCall.getInstructorInstructionalSupportPreferences());
            }
        }

        return preferences;
    }
}
