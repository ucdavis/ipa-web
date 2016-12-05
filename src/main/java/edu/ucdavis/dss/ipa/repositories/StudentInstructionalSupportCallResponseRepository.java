package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.StudentInstructionalSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface StudentInstructionalSupportCallResponseRepository extends CrudRepository<StudentInstructionalSupportCallResponse, Long> {
    List<StudentInstructionalSupportCallResponse> findByStudentInstructionalSupportCallId (long studentInstructionalSupportCallId);

    StudentInstructionalSupportCallResponse findById (long studentInstructionalSupportCallResponseId);
}
