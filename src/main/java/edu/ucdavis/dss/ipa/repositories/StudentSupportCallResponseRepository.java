package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentSupportCallResponseRepository extends CrudRepository<StudentSupportCallResponse, Long> {
    List<StudentSupportCallResponse> findByStudentSupportCallId (long studentSupportCallId);

    StudentSupportCallResponse findById (long studentSupportCallResponseId);

    List<StudentSupportCallResponse> findByScheduleId(long scheduleId);
}
