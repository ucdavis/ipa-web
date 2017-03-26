package edu.ucdavis.dss.ipa.services.jpa;

        import edu.ucdavis.dss.ipa.entities.*;
        import edu.ucdavis.dss.ipa.repositories.InstructorSupportCallResponseRepository;
        import edu.ucdavis.dss.ipa.services.InstructorService;
        import edu.ucdavis.dss.ipa.services.InstructorSupportCallResponseService;
        import org.springframework.stereotype.Service;

        import javax.inject.Inject;
        import java.util.ArrayList;
        import java.util.List;

@Service
public class JpaInstructorSupportCallResponseService implements InstructorSupportCallResponseService {

    @Inject InstructorSupportCallResponseRepository instructorSupportCallResponseRepository;
    @Inject InstructorService instructorService;

    @Override
    public InstructorSupportCallResponse findOneById(long instructorInstructionalSupportCallResponseId) {
        return instructorSupportCallResponseRepository.findById(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public List<InstructorSupportCallResponse> findByScheduleId(long scheduleId) {
        List<InstructorSupportCallResponse> supportCallResponses = instructorSupportCallResponseRepository.findByScheduleId(scheduleId);


        return supportCallResponses;
    }

    @Override
    public void delete(long instructorInstructionalSupportCallResponseId) {
        instructorSupportCallResponseRepository.delete(instructorInstructionalSupportCallResponseId);
    }

    @Override
    public InstructorSupportCallResponse update(InstructorSupportCallResponse instructorSupportCallResponse) {
        return instructorSupportCallResponseRepository.save(instructorSupportCallResponse);
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
    public InstructorSupportCallResponse findByScheduleIdAndInstructorIdAndTermCode(long scheduleId, long instructorId, String termCode) {
        return instructorSupportCallResponseRepository.findByScheduleIdAndInstructorIdAndTermCode(scheduleId, instructorId, termCode);
    }

    @Override
    public List<InstructorSupportCallResponse> createMany(List<Long> instructorIds, InstructorSupportCallResponse instructorResponseDTO) {
        List<InstructorSupportCallResponse> instructorResponses = new ArrayList<>();

        for (Long instructorId : instructorIds) {
            Instructor instructor = instructorService.getOneById(instructorId);
            InstructorSupportCallResponse instructorResponse = new InstructorSupportCallResponse();

            instructorResponse.setSchedule(instructorResponseDTO.getSchedule());
            instructorResponse.setInstructor(instructor);
            instructorResponse.setSubmitted(false);
            instructorResponse.setMessage(instructorResponseDTO.getMessage());
            instructorResponse.setNextContactAt(instructorResponseDTO.getNextContactAt());
            instructorResponse.setTermCode(instructorResponseDTO.getTermCode());
            instructorResponse.setDueDate(instructorResponseDTO.getDueDate());

            instructorResponse = this.create(instructorResponse);
            instructorResponses.add(instructorResponse);
        }

        return instructorResponses;
    }

    @Override
    public InstructorSupportCallResponse create (InstructorSupportCallResponse instructorSupportCallResponse) {
        return instructorSupportCallResponseRepository.save(instructorSupportCallResponse);
    }
}
