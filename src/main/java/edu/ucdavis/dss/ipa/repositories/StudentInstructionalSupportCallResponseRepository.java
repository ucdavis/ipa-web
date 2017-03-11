package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentInstructionalSupportCallResponseRepository extends CrudRepository<StudentSupportCallResponse, Long> {
    List<StudentSupportCallResponse> findByStudentInstructionalSupportCallId (long studentInstructionalSupportCallId);

    StudentSupportCallResponse findById (long studentInstructionalSupportCallResponseId);
}
