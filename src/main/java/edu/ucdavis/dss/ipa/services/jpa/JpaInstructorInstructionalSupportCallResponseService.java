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
    public InstructorInstructionalSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId) {
        return instructorInstructionalSupportCallResponseRepository.findById(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public List<InstructorInstructionalSupportCallResponse> findByScheduleId(long scheduleId) {
        List<InstructorInstructionalSupportCall> scheduleSupportCalls = instructorInstructionalSupportCallService.findByScheduleId(scheduleId);
        List<InstructorInstructionalSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (InstructorInstructionalSupportCall instructorSupportCall : scheduleSupportCalls) {
            supportCallResponses.addAll(instructorSupportCall.getInstructorInstructionalSupportCallResponses());
        }

        return supportCallResponses;
    }

    @Override
    public void delete(long instructorInstructionalSupportCallResponseId) {
        instructorInstructionalSupportCallResponseRepository.delete(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public InstructorInstructionalSupportCallResponse update(InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse) {
        return instructorInstructionalSupportCallResponseRepository.save(instructorInstructionalSupportCallResponse);
    }

    @Override
    public List<InstructorInstructionalSupportCallResponse> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
        List<InstructorInstructionalSupportCallResponse> scheduleSupportCallResponses = this.findByScheduleId(scheduleId);
        List<InstructorInstructionalSupportCallResponse> filtereSupportCallResponses = new ArrayList<>();

        for (InstructorInstructionalSupportCallResponse supportCallResponse : scheduleSupportCallResponses) {
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
    public InstructorInstructionalSupportCallResponse create (InstructorInstructionalSupportCall instructorInstructionalSupportCall, Instructor instructor) {
        InstructorInstructionalSupportCallResponse instructorInstructionalSupportCallResponse = new InstructorInstructionalSupportCallResponse();

        instructorInstructionalSupportCallResponse.setInstructorInstructionalSupportCall(instructorInstructionalSupportCall);
        instructorInstructionalSupportCallResponse.setInstructor(instructor);

        return instructorInstructionalSupportCallResponseRepository.save(instructorInstructionalSupportCallResponse);
    }
}
