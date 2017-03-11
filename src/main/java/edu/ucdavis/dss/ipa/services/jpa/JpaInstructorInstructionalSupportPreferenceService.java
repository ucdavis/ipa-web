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

    public InstructorSupportPreference save(InstructorSupportPreference instructorSupportPreference) {
        return this.instructorInstructionalSupportPreferenceRepository.save(instructorSupportPreference);
    }

    @Override
    public List<Long> updatePriorities(List<Long> instructorInstructionalSupportPreferenceIds) {
        return null;
    }

    @Override
    public InstructorSupportPreference create(long instructionalSupportStaffId, long instructorId, long supportCallId, long sectionGroupId) {
        SupportStaff supportStaff = instructionalSupportStaffService.findOneById(instructionalSupportStaffId);
        Instructor instructor = instructorService.getOneById(instructorId);
        SectionGroup sectionGroup = sectionGroupService.getOneById(sectionGroupId);
        InstructorSupportCall instructorSupportCall = instructorInstructionalSupportCallService.findOneById(supportCallId);

        InstructorSupportPreference instructorSupportPreference = new InstructorSupportPreference();
        instructorSupportPreference.setSectionGroup(sectionGroup);
        instructorSupportPreference.setSupportStaff(supportStaff);
        instructorSupportPreference.setInstructor(instructor);
        instructorSupportPreference.setInstructorSupportCall(instructorSupportCall);
        // TODO: Add logic to properly check sibling preferences, determine the current lowest priority, and set priority to one below that.
        instructorSupportPreference.setPriority(1L);

        return this.save(instructorSupportPreference);
    }

    @Override
    public void delete(Long instructorInstructionalSupportPreferenceId) {
        this.instructorInstructionalSupportPreferenceRepository.deleteById(instructorInstructionalSupportPreferenceId);
    }

    @Override
    public List<InstructorSupportPreference> findByInstructorIdAndInstructorSupportCallId(long instructorId, long instructorSupportCallId) {
        return this.instructorInstructionalSupportPreferenceRepository.findByInstructorIdAndInstructorInstructionalSupportCallId(instructorId, instructorSupportCallId);
    }

    @Override
    public List<InstructorSupportPreference> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        List<InstructorSupportCall> supportCalls = instructorInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<InstructorSupportPreference> preferences = new ArrayList<>();

        for (InstructorSupportCall supportCall : supportCalls) {
            if (supportCall.getTermCode().equals(termCode)) {
                preferences.addAll(supportCall.getInstructorSupportPreferences());
            }
        }

        return preferences;
    }
}
