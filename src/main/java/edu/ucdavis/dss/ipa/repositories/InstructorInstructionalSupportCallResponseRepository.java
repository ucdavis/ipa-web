package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCallResponse;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorInstructionalSupportCallResponseRepository extends CrudRepository<InstructorInstructionalSupportCallResponse, Long> {
    List<InstructorInstructionalSupportCallResponse> findByInstructorInstructionalSupportCallId (long instructorInstructionalSupportCallId);

    InstructorInstructionalSupportCallResponse findById (long instructorInstructionalSupportCallResponseId);
}
