package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentSupportCallResponseRepository extends CrudRepository<StudentSupportCallResponse, Long> {
    StudentSupportCallResponse findById (long studentSupportCallResponseId);

    List<StudentSupportCallResponse> findByScheduleId(long scheduleId);

    StudentSupportCallResponse findByScheduleIdAndSupportStaffIdAndTermCode(long scheduleId, long supportStaffId, String termCode);

    List<StudentSupportCallResponse> findByScheduleIdAndSendEmailAndIsSubmitted(long scheduleId, boolean sendEmail, boolean isSubmitted);
}
