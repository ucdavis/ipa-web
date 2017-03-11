package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorInstructionalSupportCallResponseRepository extends CrudRepository<InstructorSupportCallResponse, Long> {
    List<InstructorSupportCallResponse> findByInstructorInstructionalSupportCallId (long instructorInstructionalSupportCallId);

    InstructorSupportCallResponse findById (long instructorInstructionalSupportCallResponseId);
}
