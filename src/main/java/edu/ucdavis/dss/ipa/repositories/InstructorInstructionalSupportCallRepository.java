package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InstructorInstructionalSupportCallRepository extends CrudRepository<InstructorInstructionalSupportCall, Long> {

    List<InstructorInstructionalSupportCall> findByScheduleId(long scheduleId);

    InstructorInstructionalSupportCall findById(long instructorInstructionalSupportCallId);
}
