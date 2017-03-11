package edu.ucdavis.dss.ipa.services.jpa;

        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.repositories.InstructorInstructionalSupportCallResponseRepository;
        import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallResponseService;
        import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallService;
        import org.springframework.stereotype.Service;

        import javax.inject.Inject;
        import java.util.ArrayList;
        import java.util.List;

@Service
public class JpaInstructorInstructionalSupportCallResponseService implements InstructorInstructionalSupportCallResponseService {

    @Inject InstructorInstructionalSupportCallResponseRepository instructorInstructionalSupportCallResponseRepository;
    @Inject InstructorInstructionalSupportCallService instructorInstructionalSupportCallService;

    @Override
    public InstructorSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId) {
        return instructorInstructionalSupportCallResponseRepository.findById(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleId(long scheduleId) {
        List<InstructorSupportCall> scheduleSupportCalls = instructorInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<InstructorSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (InstructorSupportCall instructorSupportCall : scheduleSupportCalls) {
            supportCallResponses.addAll(instructorSupportCall.getInstructorSupportCallResponses());
        }

        return supportCallResponses;
    }

    @Override
    public void delete(long instructorInstructionalSupportCallResponseId) {
        instructorInstructionalSupportCallResponseRepository.delete(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public InstructorSupportCallResponse update(InstructorSupportCallResponse instructorSupportCallResponse) {
        return instructorInstructionalSupportCallResponseRepository.save(instructorSupportCallResponse);
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
        List<InstructorSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<InstructorSupportCallResponse> filtereSupportCallResponses = new ArrayList<>();

        for (InstructorSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
            if (supportCallResponse.getInstructorIdentification() == instructorId) {
                filtereSupportCallResponses.add(supportCallResponse);
            }
        }

        return filtereSupportCallResponses;
    }

    @Override
    public void sendNotificationsByWorkgroupId(Long workgroupId) {

    }

    @Override
    public InstructorSupportCallResponse create (InstructorSupportCall instructorSupportCall, Instructor instructor) {
        InstructorSupportCallResponse instructorSupportCallResponse = new InstructorSupportCallResponse();

        instructorSupportCallResponse.setInstructorSupportCall(instructorSupportCall);
        instructorSupportCallResponse.setInstructor(instructor);

        return instructorInstructionalSupportCallResponseRepository.save(instructorSupportCallResponse);
    }
}
