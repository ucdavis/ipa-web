package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorSupportCallResponseRepository extends CrudRepository<InstructorSupportCallResponse, Long> {
    InstructorSupportCallResponse findById (long instructorSupportCallResponseId);

    List<InstructorSupportCallResponse> findByScheduleId (long scheduleId);

    InstructorSupportCallResponse findByScheduleIdAndInstructorIdAndTermCode(long scheduleId, long instructorId, String termCode);

    List<InstructorSupportCallResponse> findByScheduleIdAndTermCode(long scheduleId, String termCode);

    List<InstructorSupportCallResponse> findByScheduleIdAndSendEmailAndIsSubmitted(long scheduleId, boolean sendEmail, boolean isSubmitted);
}
